/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/

package org.apache.cayenne.access;

import java.util.List;

import org.apache.cayenne.CayenneException;
import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.query.BatchQuery;
import org.apache.cayenne.query.InsertBatchQuery;
import org.apache.cayenne.query.Query;
import org.apache.cayenne.util.Util;

/**
 * Used as an observer for DataContext commit operations.
 * 
 * @since 1.2
 */
class DataDomainFlushObserver implements OperationObserver {

    public void nextQueryException(Query query, Exception ex) {
        throw new CayenneRuntimeException("Raising from query exception.", Util
                .unwindException(ex));
    }

    public void nextGlobalException(Exception ex) {
        throw new CayenneRuntimeException(
                "Raising from underlyingQueryEngine exception.",
                Util.unwindException(ex));
    }

    /**
     * Processes generated keys.
     * 
     * @since 1.2
     */
    public void nextGeneratedDataRows(Query query, ResultIterator keysIterator) {

        // read and close the iterator before doing anything else
        List<DataRow> keys;
        try {
            keys = (List<DataRow>) keysIterator.allRows();
        }
        catch (CayenneException ex) {
            throw new CayenneRuntimeException("Error reading primary key", Util
                    .unwindException(ex));
        }
        finally {
            try {
                keysIterator.close();
            }
            catch (CayenneException e) {
                throw new CayenneRuntimeException(
                        "Error closing primary key result set",
                        Util.unwindException(e));
            }
        }

        if (!(query instanceof InsertBatchQuery)) {
            throw new CayenneRuntimeException(
                    "Generated keys only supported for InsertBatchQuery, instead got "
                            + query);
        }

        BatchQuery batch = (BatchQuery) query;

        ObjectId id = batch.getObjectId();
        if (id == null || !id.isTemporary()) {
            // why would this happen?
            return;
        }

        if (keys.size() != 1) {
            throw new CayenneRuntimeException(
                    "One and only one PK row is expected, instead got " + keys.size());
        }

        DataRow key = keys.get(0);

        // empty key?
        if (key.size() == 0) {
            throw new CayenneRuntimeException("Empty key generated.");
        }

        // determine DbAttribute name...

        // As of now (01/2005) all tested drivers don't provide decent descriptors of
        // identity result sets, so a data row will contain garbage labels. Also most
        // DBs only support one autogenerated key per table... So here we will have to
        // infer the key name and currently will only support a single column...
        if (key.size() > 1) {
            throw new CayenneRuntimeException(
                    "Only a single column autogenerated PK is supported. "
                            + "Generated key: "
                            + key);
        }

        for (DbAttribute attribute : batch.getDbEntity().getGeneratedAttributes()) {

            // batch can have generated attributes that are not PKs, e.g. columns with
            // DB DEFAULT values. Ignore those.
            if (attribute.isPrimaryKey()) {
                Object value = key.values().iterator().next();

                // Log the generated PK
                QueryLogger.logGeneratedKey(attribute, value);

                // I guess we should override any existing value,
                // as generated key is the latest thing that exists in the DB.
                id.getReplacementIdMap().put(attribute.getName(), value);
                break;
            }
        }
    }

    public void nextBatchCount(Query query, int[] resultCount) {
    }

    public void nextCount(Query query, int resultCount) {
    }

    public void nextDataRows(Query query, List<DataRow> dataRows) {
    }

    public void nextDataRows(Query q, ResultIterator it) {
        throw new UnsupportedOperationException(
                "'nextDataRows(Query,ResultIterator)' is unsupported (and unexpected) on commit.");
    }

    public boolean isIteratedResult() {
        return false;
    }
}
