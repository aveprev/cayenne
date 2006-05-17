/* ====================================================================
 * 
 * The ObjectStyle Group Software License, version 1.1
 * ObjectStyle Group - http://objectstyle.org/
 * 
 * Copyright (c) 2002-2005, Andrei (Andrus) Adamchik and individual authors
 * of the software. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any,
 *    must include the following acknowlegement:
 *    "This product includes software developed by independent contributors
 *    and hosted on ObjectStyle Group web site (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The names "ObjectStyle Group" and "Cayenne" must not be used to endorse
 *    or promote products derived from this software without prior written
 *    permission. For written permission, email
 *    "andrus at objectstyle dot org".
 * 
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    or "Cayenne", nor may "ObjectStyle" or "Cayenne" appear in their
 *    names without prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE OBJECTSTYLE GROUP OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals and hosted on ObjectStyle Group web site.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 */
package org.objectstyle.cayenne.access.jdbc;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectstyle.cayenne.CayenneException;
import org.objectstyle.cayenne.CayenneRuntimeException;
import org.objectstyle.cayenne.DataRow;
import org.objectstyle.cayenne.access.ResultIterator;
import org.objectstyle.cayenne.access.types.ExtendedType;
import org.objectstyle.cayenne.map.DbAttribute;
import org.objectstyle.cayenne.map.DbEntity;
import org.objectstyle.cayenne.util.Util;

/**
 * A ResultIterator over the underlying JDBC ResultSet.
 * 
 * @since 1.2
 * @author Andrus Adamchik
 */
// Replaces DefaultResultIterator
public class JDBCResultIterator implements ResultIterator {

    // Connection information
    protected Connection connection;
    protected Statement statement;
    protected ResultSet resultSet;

    protected RowDescriptor rowDescriptor;

    DataRowPostProcessor postProcessor;

    // last indexed PK
    protected DbEntity rootEntity;
    protected int[] pkIndices;

    protected int mapCapacity;

    protected boolean closingConnection;
    protected boolean closed;

    protected boolean nextRow;
    protected int fetchedSoFar;
    protected int fetchLimit;

    /**
     * Creates new JDBCResultIterator that reads from provided ResultSet.
     */
    public JDBCResultIterator(Connection connection, Statement statement,
            ResultSet resultSet, RowDescriptor descriptor, int fetchLimit)
            throws CayenneException {

        this.connection = connection;
        this.statement = statement;
        this.resultSet = resultSet;
        this.rowDescriptor = descriptor;
        this.fetchLimit = fetchLimit;

        this.mapCapacity = (int) Math.ceil((descriptor.getWidth()) / 0.75);

        checkNextRow();
    }

    /**
     * Returns all unread data rows from ResultSet, closing this iterator if needed.
     */
    public List dataRows(boolean close) throws CayenneException {
        List list = new ArrayList();

        try {
            while (this.hasNextRow()) {
                list.add(this.nextDataRow());
            }
        }
        finally {
            if (close) {
                this.close();
            }
        }

        return list;
    }

    /**
     * Returns true if there is at least one more record that can be read from the
     * iterator.
     */
    public boolean hasNextRow() {
        return nextRow;
    }

    /**
     * Returns the next result row as a Map.
     */
    public Map nextDataRow() throws CayenneException {
        if (!hasNextRow()) {
            throw new CayenneException(
                    "An attempt to read uninitialized row or past the end of the iterator.");
        }

        // read
        Map row = readDataRow();

        // rewind
        checkNextRow();

        return row;
    }

    /**
     * Returns a map of ObjectId values from the next result row. Primary key columns are
     * determined from the provided DbEntity.
     */
    public Map nextObjectId(DbEntity entity) throws CayenneException {
        if (!hasNextRow()) {
            throw new CayenneException(
                    "An attempt to read uninitialized row or past the end of the iterator.");
        }

        // index id
        if (rootEntity != entity || pkIndices == null) {
            this.rootEntity = entity;
            indexPK();
        }

        // read ...
        // TODO: note a mismatch with 1.1 API - ID positions are preset and are
        // not affected by the entity specified (think of deprecating/replacing this)
        Map row = readIdRow();

        // rewind
        checkNextRow();

        return row;
    }

    public void skipDataRow() throws CayenneException {
        if (!hasNextRow()) {
            throw new CayenneException(
                    "An attempt to read uninitialized row or past the end of the iterator.");
        }

        checkNextRow();
    }

    /**
     * Closes ResultIterator and associated ResultSet. This method must be called
     * explicitly when the user is finished processing the records. Otherwise unused
     * database resources will not be released properly.
     */
    public void close() throws CayenneException {
        if (!closed) {

            nextRow = false;

            StringWriter errors = new StringWriter();
            PrintWriter out = new PrintWriter(errors);

            try {
                resultSet.close();
            }
            catch (SQLException e1) {
                out.println("Error closing ResultSet");
                e1.printStackTrace(out);
            }

            if (statement != null) {
                try {
                    statement.close();
                }
                catch (SQLException e2) {
                    out.println("Error closing PreparedStatement");
                    e2.printStackTrace(out);
                }
            }

            // TODO: andrus, 5/8/2006 - closing connection within JDBCResultIterator is
            // obsolete as this is bound to transaction closing in DataContext. Deprecate
            // this after 1.2
            
            // close connection, if this object was explicitly configured to be
            // responsible for doing it
            if (connection != null && isClosingConnection()) {
                try {
                    connection.close();
                }
                catch (SQLException e3) {
                    out.println("Error closing Connection");
                    e3.printStackTrace(out);
                }
            }

            try {
                out.close();
                errors.close();
            }
            catch (IOException ioex) {
                // ignore - this is never going to happen, after all we are writing to
                // StringBuffer in memory
            }

            StringBuffer buf = errors.getBuffer();
            if (buf.length() > 0) {
                throw new CayenneException("Error closing ResultIterator: " + buf);
            }

            closed = true;
        }
    }

    /**
     * Returns the number of columns in the result row.
     */
    public int getDataRowWidth() {
        return rowDescriptor.getWidth();
    }

    /**
     * Moves internal ResultSet cursor position down one row. Checks if the next row is
     * available.
     */
    protected void checkNextRow() throws CayenneException {
        nextRow = false;
        try {
            if ((fetchLimit <= 0 || fetchedSoFar < fetchLimit) && resultSet.next()) {
                nextRow = true;
                fetchedSoFar++;
            }
        }
        catch (SQLException e) {
            throw new CayenneException("Error rewinding ResultSet", e);
        }
    }

    /**
     * Reads a row from the internal ResultSet at the current cursor position.
     */
    protected Map readDataRow() throws CayenneException {
        try {
            DataRow dataRow = new DataRow(mapCapacity);
            ExtendedType[] converters = rowDescriptor.getConverters();
            ColumnDescriptor[] columns = rowDescriptor.getColumns();
            int resultWidth = rowDescriptor.getWidth();

            // process result row columns,
            for (int i = 0; i < resultWidth; i++) {
                // note: jdbc column indexes start from 1, not 0 unlike everywhere else
                Object val = converters[i].materializeObject(resultSet, i + 1, columns[i]
                        .getJdbcType());
                dataRow.put(columns[i].getLabel(), val);
            }
            
            if(postProcessor != null) {
                postProcessor.postprocessRow(resultSet, dataRow);
            }

            return dataRow;
        }
        catch (CayenneException cex) {
            // rethrow unmodified
            throw cex;
        }
        catch (Exception otherex) {
            throw new CayenneException("Exception materializing column.", Util
                    .unwindException(otherex));
        }
    }

    /**
     * Reads a row from the internal ResultSet at the current cursor position, processing
     * only columns that are part of the ObjectId of a target class.
     */
    protected Map readIdRow() throws CayenneException {
        try {
            DataRow idRow = new DataRow(2);
            ExtendedType[] converters = rowDescriptor.getConverters();
            ColumnDescriptor[] columns = rowDescriptor.getColumns();
            int len = pkIndices.length;

            for (int i = 0; i < len; i++) {

                // dereference column index
                int index = pkIndices[i];

                // note: jdbc column indexes start from 1, not 0 as in arrays
                Object val = converters[index].materializeObject(
                        resultSet,
                        index + 1,
                        columns[index].getJdbcType());
                idRow.put(columns[index].getLabel(), val);
            }
            
            if(postProcessor != null) {
                postProcessor.postprocessRow(resultSet, idRow);
            }

            return idRow;
        }
        catch (CayenneException cex) {
            // rethrow unmodified
            throw cex;
        }
        catch (Exception otherex) {
            throw new CayenneException("Exception materializing id column.", Util
                    .unwindException(otherex));
        }
    }

    /**
     * Creates an index of PK columns in the RowDescriptor.
     */
    protected void indexPK() {
        if (rootEntity == null) {
            throw new CayenneRuntimeException("Null root DbEntity, can't index PK");
        }

        int len = rootEntity.getPrimaryKey().size();

        // sanity check
        if (len == 0) {
            throw new CayenneRuntimeException("Root DbEntity has no PK defined: "
                    + rootEntity);
        }

        int[] pk = new int[len];
        ColumnDescriptor[] columns = rowDescriptor.getColumns();
        for (int i = 0, j = 0; i < columns.length; i++) {
            DbAttribute a = (DbAttribute) rootEntity.getAttribute(columns[i].getName());
            if (a != null && a.isPrimaryKey()) {
                pk[j++] = i;
            }
        }

        this.pkIndices = pk;
    }

    /**
     * Returns <code>true</code> if this iterator is responsible for closing its
     * connection, otherwise a user of the iterator must close the connection after
     * closing the iterator.
     */
    public boolean isClosingConnection() {
        return closingConnection;
    }

    /**
     * Sets the <code>closingConnection</code> property.
     */
    public void setClosingConnection(boolean flag) {
        this.closingConnection = flag;
    }

    public RowDescriptor getRowDescriptor() {
        return rowDescriptor;
    }

    void setPostProcessor(DataRowPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }
}