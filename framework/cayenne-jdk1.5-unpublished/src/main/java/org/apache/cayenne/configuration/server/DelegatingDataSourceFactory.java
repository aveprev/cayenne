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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.configuration.AdhocObjectFactory;
import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.configuration.RuntimeProperties;
import org.apache.cayenne.di.BeforeScopeEnd;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.spi.ScopeEventBinding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A {@link DataSourceFactory} that delegates DataSource creation to another factory,
 * which is determined dynamically per DataNodeDescriptor. The delegate class may be
 * explicitly defined in the {@link DataNodeDescriptor}. If not, and if the descriptor has
 * a configuration resource attached to it, {@link XMLPoolingDataSourceFactory} is used.
 * <p>
 * If the environment contains properties <em>cayenne.jdbc.url.domain_name.node_name</em>
 * (or <em>cayenne.jdbc.url</em>) and <em>cayenne.jdbc.driver.domain_name.node_name</em>
 * (or <em>cayenne.jdbc.driver</em>), any DataSourceFactory configured in the
 * DataNodeDescriptor is ignored, and the {@link PropertyDataSourceFactory} is used.
 * 
 * @since 3.1
 */
public class DelegatingDataSourceFactory implements DataSourceFactory {

    private static final Log logger = LogFactory
            .getLog(DelegatingDataSourceFactory.class);

    @Inject
    protected AdhocObjectFactory objectFactory;

    @Inject
    protected RuntimeProperties properties;

    protected Map<DataSource, ScopeEventBinding> managedDataSources;

    public DelegatingDataSourceFactory() {
        managedDataSources = new ConcurrentHashMap<DataSource, ScopeEventBinding>();
    }

    public DataSource getDataSource(DataNodeDescriptor nodeDescriptor) throws Exception {
        DataSource dataSource = getDataSourceFactory(nodeDescriptor).getDataSource(
                nodeDescriptor);
        attachToScope(dataSource);
        return dataSource;
    }

    @BeforeScopeEnd
    public void shutdown() {
        for (ScopeEventBinding binding : managedDataSources.values()) {
            binding.onScopeEvent();
        }

        managedDataSources.clear();
    }

    /**
     * Ensure that DataSource implementations returned from this factory receive
     * {@link BeforeScopeEnd} events.
     */
    protected void attachToScope(DataSource dataSource) {

        // no real thread-safety here, just checking to speed up processing... in the
        // worst case we'll replace event binding for the same pool with an equivalent one
        if (!managedDataSources.containsKey(dataSource)) {

            Class<BeforeScopeEnd> annotationType = BeforeScopeEnd.class;

            // note that checking for class directly prevents wrapping... will wait till
            // Java6/JDBC4 upgrade to check for wrappers
            for (Method method : dataSource.getClass().getMethods()) {

                if (method.isAnnotationPresent(annotationType)) {
                    managedDataSources.put(dataSource, new ScopeEventBinding(
                            dataSource,
                            method));

                    // no need to look further as we are supporting only a single scope
                    // method
                    break;
                }
            }
        }
    }

    protected DataSourceFactory getDataSourceFactory(DataNodeDescriptor nodeDescriptor) {
        String typeName = null;

        if (shouldConfigureDataSourceFromProperties(nodeDescriptor)) {
            typeName = PropertyDataSourceFactory.class.getName();
        }
        else {
            typeName = nodeDescriptor.getDataSourceFactoryType();
        }

        if (typeName == null) {
            if (nodeDescriptor.getDataSourceDescriptor() == null) {
                throw new CayenneRuntimeException(
                        "DataNodeDescriptor '%s' has null 'dataSourceFactoryType' and 'dataSourceDescriptor' properties",
                        nodeDescriptor.getName());
            }

            typeName = XMLPoolingDataSourceFactory.class.getName();
        }

        return objectFactory.newInstance(DataSourceFactory.class, typeName);
    }

    protected boolean shouldConfigureDataSourceFromProperties(
            DataNodeDescriptor nodeDescriptor) {

        String channelName = nodeDescriptor.getDataChannelDescriptor() != null
                ? nodeDescriptor.getDataChannelDescriptor().getName()
                : null;

        String driver = properties.get(PropertyDataSourceFactory.JDBC_DRIVER_PROPERTY);

        if (driver == null && channelName != null) {
            driver = properties.get(PropertyDataSourceFactory.JDBC_DRIVER_PROPERTY
                    + "."
                    + nodeDescriptor.getDataChannelDescriptor().getName()
                    + "."
                    + nodeDescriptor.getName());
        }

        if (driver == null) {
            return false;
        }

        String url = properties.get(PropertyDataSourceFactory.JDBC_URL_PROPERTY);

        if (url == null && channelName != null) {
            url = properties.get(PropertyDataSourceFactory.JDBC_URL_PROPERTY
                    + "."
                    + nodeDescriptor.getDataChannelDescriptor().getName()
                    + "."
                    + nodeDescriptor.getName());
        }

        if (url == null) {
            return false;
        }

        logger
                .info(String
                        .format(
                                "Found DataSourceFactory system property overrides for URL and Driver "
                                        + "of '%s.%s' node. Will ignore project DataSource configuration.",
                                channelName,
                                nodeDescriptor.getName()));
        return true;
    }

}
