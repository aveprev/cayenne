package org.objectstyle.cayenne.dba;
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

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import org.objectstyle.cayenne.CayenneRuntimeException;
import org.objectstyle.cayenne.access.*;
import org.objectstyle.cayenne.map.DbEntity;
import org.objectstyle.cayenne.map.ObjAttribute;
import org.objectstyle.cayenne.query.*;

/** 
 * Default primary key generator implementation. Uses a lookup table named
 * "AUTO_PK_SUPPORT" to search and increment primary keys for tables.  
 * 
 * @author Andrei Adamchik
 */
public class JdbcPkGenerator implements PkGenerator {
    static Logger logObj = Logger.getLogger(JdbcPkGenerator.class.getName());

    private static final ObjAttribute[] resultDesc =
        new ObjAttribute[] { new ObjAttribute("nextId", Integer.class.getName(), null)};

    protected HashMap pkCache = new HashMap();
    protected int pkCacheSize = 10;

    /** Generates database objects to provide
     *  automatic primary key support. This implementation will create
     *  a lookup table called "AUTO_PK_SUPPORT" unless it already exists:
     * 
     <pre>
     *CREATE TABLE AUTO_PK_SUPPORT (
     *TABLE_NAME           CHAR(100) NOT NULL,
     *NEXT_ID              INTEGER NOT NULL
     *)
     *</pre>
     *
     *  @param node node that provides access to a DataSource.
     */
    public void createAutoPkSupport(DataNode node) throws Exception {
        // check if a table exists
        Connection con = node.getDataSource().getConnection();
        boolean shouldCreate = true;
        try {
            DatabaseMetaData md = con.getMetaData();
            ResultSet tables = md.getTables(null, null, "AUTO_PK_SUPPORT", null);
            shouldCreate = !tables.next();
            tables.close();
        }
        finally {
            // return connection to the pool
            con.close();
        }

        if (shouldCreate) {
            StringBuffer buf = new StringBuffer();
            buf.append("CREATE TABLE AUTO_PK_SUPPORT ").append(
                "(TABLE_NAME CHAR(100) NOT NULL, NEXT_ID INTEGER NOT NULL)");

            runSchemaUpdate(node, buf.toString());
        }
    }

    /** Will drop table named "AUTO_PK_SUPPORT" if it exists in the 
      * database. */
    public void dropAutoPkSupport(DataNode node) throws Exception {

        // check if a table exists
        Connection con = node.getDataSource().getConnection();
        ResultSet tables = null;
        boolean shouldDrop = false;
        try {
            DatabaseMetaData md = con.getMetaData();
            tables = md.getTables(null, null, "AUTO_PK_SUPPORT", null);
            shouldDrop = tables.next();
        }
        finally {
            if (tables != null) {
                tables.close();
            }

            // return connection to the pool
            con.close();
        }

        if (shouldDrop) {
            runSchemaUpdate(node, "DROP TABLE AUTO_PK_SUPPORT");
        }
    }

    /** Performs necessary database operations to do primary key generation
     *  for a particular DbEntity. This may require a prior call 
     *  to <code>createAutoPkSupport<code> method.
     * 
     * <p>This operation is "safe" in that it won't override any existing values.</p>
     * 
     *  <p>This operation may produce incorrect results if there is already data
     *  in the tables, since it will populate lookup tables
     *  with "1" instead of looking for the correct value. 
     * <i>Better implementation is pending.</i></p>
     *
     *  @param node node that provides connection layer for PkGenerator.
     *  @param dbEntity DbEntity that needs an auto PK support
     */
    public void createAutoPkSupportForDbEntity(DataNode node, DbEntity dbEntity)
        throws Exception {

        // check for existing record
        boolean shouldInsert = true;
        StringBuffer buf = new StringBuffer();
        buf
            .append("SELECT NEXT_ID FROM AUTO_PK_SUPPORT ")
            .append("WHERE TABLE_NAME = '")
            .append(dbEntity.getName())
            .append('\'');

        List rows = runSelect(node, buf.toString());
        if (rows.size() > 0) {
            shouldInsert = false;
        }

        // create new one if needed
        if (shouldInsert) {

            buf.setLength(0);
            buf
                .append("INSERT INTO AUTO_PK_SUPPORT (TABLE_NAME, NEXT_ID) ")
                .append("VALUES ('")
                .append(dbEntity.getName())
                .append("', 1)");

            runSchemaUpdate(node, buf.toString());
        }
    }

    /** 
     * Creates and executes SqlModifyQuery using inner class PkSchemaProcessor
     * to track the results of the execution.
     * 
     * @throws java.lang.Exception in case of query failure. 
     */
    public void runSchemaUpdate(DataNode node, String sql) throws Exception {
        SqlModifyQuery q = new SqlModifyQuery();
        q.setSqlString(sql);

        PkSchemaProcessor pr = new PkSchemaProcessor();
        node.performQuery(q, pr);
    }

    /** Creates and executes SqlModifyQuery using inner class PkSchemaProcessor
     * to track the results of the execution.
     * 
     * @throws java.lang.Exception in case of query failure. */
    private List runSelect(DataNode node, String sql) throws Exception {
        SqlSelectQuery q = new SqlSelectQuery();
        q.setSqlString(sql);

        SelectOperationObserver observer = new SelectOperationObserver();
        node.performQuery(q, observer);
        return observer.getResults();
    }

    /**
     * <p>Generates new (unique and non-repeating) primary key for specified 
     * dbEntity.</p>
     *
     * <p>This implementation is naive and can have problems with high 
     * volume databases, when multiple applications can use this to get 
     * a primary key value. There is a possiblity that 2 clients will 
     * recieve the same value of primary key. So database specific 
     * implementations should be created for cleaner approach (like Oracle
     * sequences, for example).</p>
     */
    public Object generatePkForDbEntity(DataNode node, DbEntity ent)
        throws Exception {
        return pkFromDatabase(node, ent).getNextPrimaryKey();
    }
    
    /** 
     * Performs primary key generation ignoring cache. This method is called 
     * internally from "generatePkForDbEntity" and then saved in cache 
     * for performance. Subclasses that implement different primary key 
     * generation solutions should override this method, not 
     * "generatePkForDbEntity".
     */
    protected PkRange pkFromDatabase(DataNode node, DbEntity ent) throws Exception {
        ArrayList queries = new ArrayList(2);

        StringBuffer b1 = new StringBuffer();
        b1
            .append("SELECT NEXT_ID FROM AUTO_PK_SUPPORT WHERE TABLE_NAME = '")
            .append(ent.getName())
            .append("'");

        SqlSelectQuery sel = new SqlSelectQuery();
        sel.setSqlString(b1.toString());
        sel.setResultDesc(resultDesc);
        queries.add(sel);

        // create dummy update 
        // it will be populated with real stuff inside DB transaction
        SqlModifyQuery upd = new SqlModifyQuery();
        queries.add(upd);

        PkRetrieveProcessor pkProcessor = new PkRetrieveProcessor(upd, ent.getName());
        node.performQueries(queries, pkProcessor);

        if (!pkProcessor.successFlag) {
            throw new CayenneRuntimeException("Error generating PK.");
        }
        else {
            int val = pkProcessor.nextId.intValue();
            PkRange range = new PkRange(val, val);
            return range;
        }
    }

    /**
     * Returns a size of the entity primary key cache.
     * 10 is default, 0 means no caching.
     */
    public int getPkCacheSize() {
        return pkCacheSize;
    }

    /**
     * Sets the size of the entity primary key cache.
     */
    public void setPkCacheSize(int pkCacheSize) {
        this.pkCacheSize = pkCacheSize;
    }

    /** OperationObserver for primary key retrieval. */
    class PkRetrieveProcessor extends DefaultOperationObserver {
        private boolean successFlag;
        private SqlModifyQuery queryTemplate;
        private Integer nextId;
        private String entName;

        public PkRetrieveProcessor(SqlModifyQuery queryTemplate, String entName) {
            this.queryTemplate = queryTemplate;
            this.entName = entName;
        }

        public boolean useAutoCommit() {
            return false;
        }

        public void nextSnapshots(Query query, List resultObjects) {
            super.nextSnapshots(query, resultObjects);

            // process selected object, issue an update query
            if (resultObjects == null || resultObjects.size() == 0)
                throw new CayenneRuntimeException(
                    "Error generating PK : entity not supported: " + entName);
            if (resultObjects.size() > 1)
                throw new CayenneRuntimeException(
                    "Error generating PK : too many rows for entity: " + entName);

            Map lastPk = (Map) resultObjects.get(0);
            nextId = (Integer) lastPk.get("nextId");
            if (nextId == null) {
                throw new CayenneRuntimeException("Error generating PK : null nextId.");
            }

            // while transaction is still in progress, modify update query that will be executed next

            StringBuffer buf = new StringBuffer();
            buf
                .append("UPDATE AUTO_PK_SUPPORT SET NEXT_ID = ")
                .append(nextId.intValue() + 1)
                .append(" WHERE TABLE_NAME = '")
                .append(entName)
                .append("' AND NEXT_ID = ")
                .append(nextId);
            queryTemplate.setSqlString(buf.toString());
        }

        public void nextCount(Query query, int resultCount) {
            super.nextCount(query, resultCount);

            if (resultCount != 1)
                throw new CayenneRuntimeException(
                    "Error generating PK : update count is wrong: " + resultCount);
        }

        public void transactionCommitted() {
            super.transactionCommitted();
            successFlag = true;
        }

        public void nextQueryException(Query query, Exception ex) {
            super.nextQueryException(query, ex);
            String entityName = (query != null) ? query.getObjEntityName() : null;
            throw new CayenneRuntimeException(
                "Error generating PK for entity '" + entityName + "'.",
                ex);
        }

        public void nextGlobalException(Exception ex) {
            super.nextGlobalException(ex);
            throw new CayenneRuntimeException("Error generating PK.", ex);
        }
    }

    /** OperationObserver for schema operations. */
    class PkSchemaProcessor extends DefaultOperationObserver {
        public void nextQueryException(Query query, Exception ex) {
            super.nextQueryException(query, ex);
            String entityName = (query != null) ? query.getObjEntityName() : null;
            throw new CayenneRuntimeException(
                "Error running operation '" + entityName + "'.",
                ex);
        }

        public void nextGlobalException(Exception ex) {
            super.nextGlobalException(ex);
            throw new CayenneRuntimeException("Error creating PK.", ex);
        }
    }

}