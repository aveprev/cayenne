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

import org.apache.cayenne.conn.DataSourceInfo;
import org.apache.cayenne.log.JdbcEventLogger;
import org.apache.cayenne.map.DbAttribute;

/**
 * QueryLogger is intended to log special events that happen whenever Cayenne interacts
 * with a database. This includes execution of generated SQL statements, result counts,
 * connection events, etc. Normally QueryLogger methods are not invoked directly by the .
 * Rather it is a single logging point used by the framework.
 * <p>
 * Internally QueryLogger uses commons-logging at the "info" level.
 * </p>
 */
@Deprecated
public class QueryLogger {

    private static JdbcEventLogger logger;

    public static void setLogger(JdbcEventLogger logger) {
        QueryLogger.logger = logger;
    }

    /**
     * @since 1.2
     */
    @SuppressWarnings("unchecked")
    static ThreadLocal logLevel = new ThreadLocal();

    /**
     * Appends SQL literal for the specified object to the buffer. This is a utility
     * method and is not intended to build SQL queries, rather this is used in logging
     * routines. In particular it will trim large values to avoid flooding the logs. </p>
     * 
     * @param buffer buffer to append value
     * @param object object to be transformed to SQL literal.
     */
    public static void sqlLiteralForObject(StringBuffer buffer, Object object) {
        logger.sqlLiteralForObject(buffer, object);
    }

    /**
     * @since 1.2 logs an arbitrary message using logging level setup for QueryLogger.
     */
    public static void log(String message) {
        logger.log(message);
    }

    /**
     * Logs database connection event using container data source.
     * 
     * @since 1.2
     */
    public static void logConnect(String dataSource) {
        logger.logConnect(dataSource);
    }

    /**
     * @since 1.2
     */
    public static void logConnect(String url, String userName, String password) {
        logger.logConnect(url, userName, password);
    }

    /**
     * Logs database connection event.
     * 
     * @since 1.2
     */
    public static void logPoolCreated(DataSourceInfo dsi) {
        logger.logPoolCreated(dsi);
    }

    /**
     * @since 1.2
     */
    public static void logConnectSuccess() {
        logger.logConnectSuccess();
    }

    /**
     * @since 1.2
     */
    public static void logConnectFailure(Throwable th) {
        logger.logConnectFailure(th);
    }

    /**
     * @since 3.0
     */
    public static void logGeneratedKey(DbAttribute attribute, Object value) {
        logger.logGeneratedKey(attribute, value);
    }

    /**
     * @since 1.2
     */
    public static void logQuery(String queryStr, List<?> params) {
        logger.logQuery(queryStr, params);
    }

    /**
     * Log query content using Log4J Category with "INFO" priority.
     * 
     * @param queryStr Query SQL string
     * @param attrs optional list of DbAttribute (can be null)
     * @param params optional list of query parameters that are used when executing query
     *            in prepared statement.
     * @since 1.2
     */
    public static void logQuery(
            String queryStr,
            List<DbAttribute> attrs,
            List<?> params,
            long time) {
        logger.logQuery(queryStr, attrs, params, time);
    }

    /**
     * @since 1.2
     */
    public static void logQueryParameters(
            String label,
            List<DbAttribute> attrs,
            List<Object> parameters,
            boolean isInserting) {
        logger.logQueryParameters(label, attrs, parameters, isInserting);
    }

    /**
     * @since 1.2
     */
    public static void logSelectCount(int count) {
        logger.logSelectCount(count);
    }

    /**
     * @since 1.2
     */
    public static void logSelectCount(int count, long time) {
        logger.logSelectCount(count, time);
    }

    /**
     * @since 1.2
     */
    public static void logUpdateCount(int count) {
        logger.logUpdateCount(count);
    }

    /**
     * @since 1.2
     */
    public static void logBeginTransaction(String transactionLabel) {
        logger.logBeginTransaction(transactionLabel);
    }

    /**
     * @since 1.2
     */
    public static void logCommitTransaction(String transactionLabel) {
        logger.logCommitTransaction(transactionLabel);
    }

    /**
     * @since 1.2
     */
    public static void logRollbackTransaction(String transactionLabel) {
        logger.logRollbackTransaction(transactionLabel);
    }

    /**
     * @since 1.2
     */
    public static void logQueryError(Throwable th) {
        logger.logQueryError(th);
    }

    /**
     * @since 1.2
     */
    public static void logQueryStart(int count) {
        logger.logQueryStart(count);
    }

    /**
     * Returns true if current thread default log level is high enough for QueryLogger to
     * generate output.
     * 
     * @since 1.2
     */
    public static boolean isLoggable() {
        return logger.isLoggable();
    }
}
