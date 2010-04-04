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
package org.apache.cayenne.configuration.server;

import javax.sql.DataSource;

import org.apache.cayenne.ConfigurationException;
import org.apache.cayenne.access.ConnectionLogger;
import org.apache.cayenne.access.QueryLogger;
import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.conn.PoolManager;

/**
 * A DataSourceFactrory that creates a DataSource based on system properties. Properties
 * can be set per domain/node name or globally, aplying to all nodes without explicit
 * property set. The following properties are supported:
 * <ul>
 * <li>cayenne.jdbc.driver[.domain_name.node_name]
 * <li>cayenne.jdbc.url[.domain_name.node_name]
 * <li>cayenne.adapter[.domain_name.node_name]
 * <li>cayenne.jdbc.username[.domain_name.node_name]
 * <li>cayenne.jdbc.password[.domain_name.node_name]
 * <li>cayenne.jdbc.min.connections[.domain_name.node_name]
 * <li>cayenne.jdbc.max.conections[.domain_name.node_name]
 * </ul>
 * At least url and driver properties must be specified for this factory to return a valid
 * DataSource.
 * 
 * @since 3.1
 */
public class PropertyDataSourceFactory implements DataSourceFactory {

    static final String JDBC_DRIVER_PROPERTY = "cayenne.jdbc.driver";
    static final String JDBC_URL_PROPERTY = "cayenne.jdbc.url";
    static final String JDBC_USERNAME_PROPERTY = "cayenne.jdbc.username";
    static final String JDBC_PASSWORD_PROPERTY = "cayenne.jdbc.password";
    static final String JDBC_MIN_CONNECTIONS_PROPERTY = "cayenne.jdbc.min.connections";
    static final String JDBC_MAX_CONNECTIONS_PROPERTY = "cayenne.jdbc.max.conections";

    public DataSource getDataSource(DataNodeDescriptor nodeDescriptor) throws Exception {

        String suffix = "."
                + nodeDescriptor.getDataChannelDescriptor().getName()
                + "."
                + nodeDescriptor.getName();

        String driver = getProperty(JDBC_DRIVER_PROPERTY, suffix);
        String url = getProperty(JDBC_URL_PROPERTY, suffix);
        String username = getProperty(JDBC_USERNAME_PROPERTY, suffix);
        String password = getProperty(JDBC_PASSWORD_PROPERTY, suffix);
        int minConnections = getIntProperty(JDBC_MIN_CONNECTIONS_PROPERTY, suffix, 1);
        int maxConnections = getIntProperty(JDBC_MAX_CONNECTIONS_PROPERTY, suffix, 1);

        ConnectionLogger logger = new ConnectionLogger();
        try {
            return new PoolManager(
                    driver,
                    url,
                    minConnections,
                    maxConnections,
                    username,
                    password,
                    logger);
        }
        catch (Exception e) {
            QueryLogger.logConnectFailure(e);
            throw e;
        }
    }

    protected int getIntProperty(String propertyName, String suffix, int defaultValue) {
        String string = getProperty(propertyName, suffix);

        if (string == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException e) {
            throw new ConfigurationException(
                    "Invalid int property '%s': '%s'",
                    propertyName,
                    string);
        }
    }

    protected String getProperty(String propertyName, String suffix) {
        String value = getProperty(propertyName + suffix);
        return value != null ? value : getProperty(propertyName);
    }

    /**
     * Returns a property value for a given full key. This implementation returns a System
     * property. Subclasses may lookup properties elsewhere. E.g. overriding this method
     * can help with unit testing this class.
     */
    protected String getProperty(String key) {
        return System.getProperty(key);
    }
}
