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
package org.apache.cayenne.log;

import java.util.List;

import org.apache.cayenne.conn.DataSourceInfo;
import org.apache.cayenne.map.DbAttribute;

public interface JdbcEventLogger {

    void sqlLiteralForObject(StringBuffer buffer, Object object);

    void log(String message);

    void logConnect(String dataSource);

    void logConnect(String url, String userName, String password);

    void logPoolCreated(DataSourceInfo dsi);

    void logConnectSuccess();

    void logConnectFailure(Throwable th);

    void logGeneratedKey(DbAttribute attribute, Object value);

    void logQuery(String queryStr, List<?> params);

    void logQuery(String queryStr, List<DbAttribute> attrs, List<?> params, long time);

    void logQueryParameters(
            String label,
            List<DbAttribute> attrs,
            List<Object> parameters,
            boolean isInserting);

    void logSelectCount(int count);

    void logSelectCount(int count, long time);

    void logUpdateCount(int count);

    void logBeginTransaction(String transactionLabel);

    void logCommitTransaction(String transactionLabel);

    void logRollbackTransaction(String transactionLabel);

    void logQueryError(Throwable th);

    void logQueryStart(int count);

    boolean isLoggable();
}
