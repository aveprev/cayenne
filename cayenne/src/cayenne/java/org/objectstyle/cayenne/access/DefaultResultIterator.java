/* ====================================================================
 * 
 * The ObjectStyle Group Software License, Version 1.0 
 *
 * Copyright (c) 2002 The ObjectStyle Group 
 * and individual authors of the software.  All rights reserved.
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        ObjectStyle Group (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "ObjectStyle Group" and "Cayenne" 
 *    must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact andrus@objectstyle.org.
 *
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    nor may "ObjectStyle" appear in their names without prior written
 *    permission of the ObjectStyle Group.
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
 * individuals on behalf of the ObjectStyle Group.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 *
 */
package org.objectstyle.cayenne.access;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectstyle.cayenne.CayenneException;
import org.objectstyle.cayenne.access.trans.SelectQueryAssembler;
import org.objectstyle.cayenne.access.types.ExtendedType;
import org.objectstyle.cayenne.access.types.ExtendedTypeMap;
import org.objectstyle.cayenne.dba.DbAdapter;
import org.objectstyle.cayenne.map.DbAttribute;
import org.objectstyle.cayenne.map.DbEntity;

/**
 * Default implementation of ResultIterator interface. Serves as a 
 * factory that creates data rows from <code>java.sql.ResultSet</code>.
 * 
 * <p><i>For more information see <a href="../../../../../../userguide/index.html"
 * target="_top">Cayenne User Guide.</a></i></p>
 * 
 * @author Andrei Adamchik
 */
public class DefaultResultIterator implements ResultIterator {
    static Logger logObj = Logger.getLogger(DefaultResultIterator.class);

    protected PreparedStatement prepStmt;
    protected ResultSet resultSet;
    protected Connection connection;

    protected DbAttribute[] rowDescriptor;
    protected ExtendedType[] converters;
    protected int[] idIndex;

    protected int resultSize;

    protected boolean closingConnection;
    protected boolean isClosed;

    protected boolean nextRow;

    /** 
     * Creates new DefaultResultIterator. Initializes it
     * with ResultSet and query metadata.
     */
    public DefaultResultIterator(
        PreparedStatement prepStmt,
        DbAdapter adapter,
        SelectQueryAssembler assembler)
        throws SQLException, CayenneException {

        this.prepStmt = prepStmt;
        this.connection = assembler.getCon();
        this.resultSet = prepStmt.executeQuery();
        this.rowDescriptor = assembler.getSnapshotDesc(resultSet);

        String[] rowTypes = assembler.getResultTypes(resultSet);

        resultSize = rowDescriptor.length;
        
        // this list will hold positions of PK atributes
        ArrayList idIndexList = new ArrayList(resultSize);

        converters = new ExtendedType[resultSize];
        ExtendedTypeMap typeMap = adapter.getTypeConverter();
        
        for (int i = 0; i < resultSize; i++) {
        	// index id columns
        	if(rowDescriptor[i].isPrimaryKey()) {
        		idIndexList.add(new Integer(i));
        	}
        	
        	// initialize converters
            converters[i] = typeMap.getRegisteredType(rowTypes[i]);
        }

        int indexSize = idIndexList.size();
        idIndex = new int[indexSize];
        for(int i = 0; i < indexSize; i++) {
        	idIndex[i] = ((Integer)idIndexList.get(i)).intValue();
        }
        
        init(assembler);
    }

    /**
     * Reads the first row of data.
     */
    protected void init(SelectQueryAssembler assembler)
        throws SQLException, CayenneException {
        checkNextRow();
    }

    /** 
     * Moves internal ResultSet cursor position down one row. 
     * Checks if the next row is available.
     */
    protected void checkNextRow() throws SQLException, CayenneException {
        nextRow = false;
        if (resultSet.next()) {
            nextRow = true;
        }
    }

    /** 
     * Returns true if there is at least one more record
     * that can be read from the iterator.
     */
    public boolean hasNextRow() {
        return nextRow;
    }

    /** 
     * Returns the next result row as a Map.
     */
    public Map nextDataRow() throws CayenneException {
        if (!hasNextRow()) {
            throw new CayenneException("An attempt to read uninitialized row or past the end of the iterator.");
        }

        try {
            // read
            Map row = readDataRow();

            // rewind
            checkNextRow();

            return row;
        } catch (SQLException sqex) {
            throw new CayenneException("Exception reading ResultSet.", sqex);
        }
    }

    /**
     * Returns all unread data rows from ResultSet and closes
     * this iterator. 
     */
    public List dataRows() throws CayenneException {
        ArrayList list = new ArrayList();

        try {
            while (this.hasNextRow()) {
                list.add(this.nextDataRow());
            }
            return list;
        } finally {
            this.close();
        }
    }

    /** 
     * Reads a row from the internal ResultSet at the current
     * cursor position.
     */
    protected Map readDataRow() throws SQLException, CayenneException {
        try {
            Map dataRow = new HashMap();

            // process result row columns,
            for (int i = 0; i < resultSize; i++) {
                // note: jdbc column indexes start from 1, not 0 as in arrays
                Object val =
                    converters[i].materializeObject(
                        resultSet,
                        i + 1,
                        rowDescriptor[i].getType());
                dataRow.put(rowDescriptor[i].getName(), val);
            }

            return dataRow;
        } catch (CayenneException cex) {
            // rethrow unmodified
            throw cex;
        } catch (Exception otherex) {
            logObj.warn("Error", otherex);
            throw new CayenneException("Exception materializing column.", otherex);
        }
    }

    /** 
     * Reads a row from the internal ResultSet at the current
     * cursor position.
     */
    protected Map readIdRow() throws SQLException, CayenneException {
        try {
            Map idRow = new HashMap();
            
            int len = idIndex.length;

            for (int i = 0; i < len; i++) {
            	
            	// dereference column index
            	int index = idIndex[i];
            	
                // note: jdbc column indexes start from 1, not 0 as in arrays
                Object val =
                    converters[index].materializeObject(
                        resultSet,
                        index + 1,
                        rowDescriptor[index].getType());
                idRow.put(rowDescriptor[index].getName(), val);
            }

            return idRow;
        } catch (CayenneException cex) {
            // rethrow unmodified
            throw cex;
        } catch (Exception otherex) {
            logObj.warn("Error", otherex);
            throw new CayenneException("Exception materializing id column.", otherex);
        }
    }


    /** 
     * Closes ResultIterator and associated ResultSet. This method must be
     * called explicitly when the user is finished processing the records.
     * Otherwise unused database resources will not be released properly.
     */
    public void close() throws CayenneException {
        if (!isClosed) {

            nextRow = false;

            StringWriter errors = new StringWriter();
            PrintWriter out = new PrintWriter(errors);

            try {
                resultSet.close();
            } catch (SQLException e1) {
                out.println("Error closing ResultSet");
                e1.printStackTrace(out);
            }

            try {
                prepStmt.close();
            } catch (SQLException e2) {
                out.println("Error closing PreparedStatement");
                e2.printStackTrace(out);
            }

            if (this.isClosingConnection()) {
                try {
                    connection.close();
                } catch (SQLException e3) {
                    out.println("Error closing Connection");
                    e3.printStackTrace(out);
                }
            }

            try {
                out.close();
                errors.close();
            } catch (IOException ioex) {
                // this is totally unexpected, 
                // after all we are writing to the StringBuffer
                // in the memory	
            }
            StringBuffer buf = errors.getBuffer();

            if (buf.length() > 0) {
                throw new CayenneException("Error closing ResultIterator: " + buf);
            }

            isClosed = true;
        }
    }

    /**
     * Returns <code>true</code> if this iterator is responsible 
     * for closing its connection, otherwise a user of the iterator 
     * must close the connection after closing the iterator.
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

    /**
     * @see org.objectstyle.cayenne.access.ResultIterator#skipDataRow()
     */
    public void skipDataRow() throws CayenneException {
        if (!hasNextRow()) {
            throw new CayenneException("An attempt to read uninitialized row or past the end of the iterator.");
        }

        try {
            checkNextRow();
        } catch (SQLException sqex) {
            throw new CayenneException("Exception reading ResultSet.", sqex);
        }
    }

    /**
     * Reads just ObjectId columns and returns them as a map.
     * 
     * @see org.objectstyle.cayenne.access.ResultIterator#nextObjectId()
     */
    public Map nextObjectId() throws CayenneException {
        if (!hasNextRow()) {
            throw new CayenneException("An attempt to read uninitialized row or past the end of the iterator.");
        }

        try {
            // read
            Map row = readIdRow();

            // rewind
            checkNextRow();

            return row;
        } catch (SQLException sqex) {
            throw new CayenneException("Exception reading ResultSet.", sqex);
        }
    }
}
