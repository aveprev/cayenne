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

import java.util.Collection;

import javax.sql.DataSource;

import org.apache.cayenne.ConfigurationException;
import org.apache.cayenne.DataChannel;
import org.apache.cayenne.access.DataDomain;
import org.apache.cayenne.access.DataNode;
import org.apache.cayenne.access.dbsync.SchemaUpdateStrategy;
import org.apache.cayenne.configuration.AdhocObjectFactory;
import org.apache.cayenne.configuration.ConfigurationTree;
import org.apache.cayenne.configuration.DataChannelDescriptor;
import org.apache.cayenne.configuration.DataChannelDescriptorLoader;
import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.configuration.RuntimeProperties;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Provider;
import org.apache.cayenne.event.EventManager;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.EntitySorter;
import org.apache.cayenne.resource.Resource;
import org.apache.cayenne.resource.ResourceLocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A {@link DataChannel} provider that provides a single instance of DataDomain configured
 * per configuration supplied via injected {@link DataChannelDescriptorLoader}.
 * 
 * @since 3.1
 */
public class DataDomainProvider implements Provider<DataDomain> {

    private static Log logger = LogFactory.getLog(DataDomainProvider.class);

    @Inject
    protected ResourceLocator resourceLocator;

    @Inject
    protected DataChannelDescriptorLoader loader;

    @Inject
    protected RuntimeProperties configurationProperties;

    @Inject
    protected SchemaUpdateStrategy defaultSchemaUpdateStrategy;

    @Inject
    protected DbAdapterFactory adapterFactory;

    @Inject
    protected DataSourceFactoryLoader dataSourceFactoryLoader;

    @Inject
    protected AdhocObjectFactory objectFactory;

    @Inject
    protected Injector injector;

    public DataDomain get() throws ConfigurationException {

        try {
            return createAndInitDataDomain();
        }
        catch (ConfigurationException e) {
            throw e;
        }
        catch (Exception e) {
            throw new DataDomainLoadException("Error loading DataChannel: '%s'", e, e
                    .getMessage());
        }
    }
    
    protected DataDomain createDataDomain(String name) {
        return new DataDomain(name);
    }

    protected DataDomain createAndInitDataDomain() throws Exception {
        String configurationLocation = configurationProperties
                .get(ServerModule.CONFIGURATION_LOCATION);

        if (configurationLocation == null) {
            throw new DataDomainLoadException(
                    "No configuration location available. "
                            + "You can specify when creating Cayenne runtime "
                            + "or via a system property '%s'",
                    ServerModule.CONFIGURATION_LOCATION);
        }

        long t0 = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug("starting configuration loading: " + configurationLocation);
        }

        Collection<Resource> configurations = resourceLocator
                .findResources(configurationLocation);

        if (configurations.isEmpty()) {
            throw new DataDomainLoadException(
                    "Configuration file \"%s\" is not found.",
                    configurationLocation);
        }

        Resource configurationResource = configurations.iterator().next();

        // no support for multiple configs yet, but this is not a hard error
        if (configurations.size() > 1) {
            logger.info("found "
                    + configurations.size()
                    + " configurations, will use the first one: "
                    + configurationResource.getURL());
        }

        ConfigurationTree<DataChannelDescriptor> tree = loader
                .load(configurationResource);
        if (!tree.getLoadFailures().isEmpty()) {
            // TODO: andrus 03/10/2010 - log the errors before throwing?
            throw new DataDomainLoadException(tree, "Error loading DataChannelDescriptor");
        }

        long t1 = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
            logger.debug("finished configuration loading: "
                    + configurationLocation
                    + " in "
                    + (t1 - t0)
                    + " ms.");
        }

        DataChannelDescriptor descriptor = tree.getRootNode();
        DataDomain dataDomain = createDataDomain(descriptor.getName());
        
        dataDomain.setEntitySorter(injector.getInstance(EntitySorter.class));
        dataDomain.setEventManager(injector.getInstance(EventManager.class));

        dataDomain.initWithProperties(descriptor.getProperties());

        for (DataMap dataMap : descriptor.getDataMaps()) {
            dataDomain.addDataMap(dataMap);
        }

        for (DataNodeDescriptor nodeDescriptor : descriptor.getNodeDescriptors()) {
            DataNode dataNode = new DataNode(nodeDescriptor.getName());

            dataNode.setDataSourceLocation(nodeDescriptor.getParameters());

            DataSourceFactory dataSourceFactory = dataSourceFactoryLoader
                    .getDataSourceFactory(nodeDescriptor);

            DataSource dataSource = dataSourceFactory.getDataSource(nodeDescriptor);

            dataNode.setDataSourceFactory(nodeDescriptor.getDataSourceFactoryType());
            dataNode.setDataSource(dataSource);

            // schema update strategy
            String schemaUpdateStrategyType = nodeDescriptor
                    .getSchemaUpdateStrategyType();

            if (schemaUpdateStrategyType == null) {
                dataNode.setSchemaUpdateStrategy(defaultSchemaUpdateStrategy);
                dataNode.setSchemaUpdateStrategyName(defaultSchemaUpdateStrategy
                        .getClass()
                        .getName());
            }
            else {
                SchemaUpdateStrategy strategy = objectFactory.newInstance(
                        SchemaUpdateStrategy.class,
                        schemaUpdateStrategyType);
                dataNode.setSchemaUpdateStrategyName(schemaUpdateStrategyType);
                dataNode.setSchemaUpdateStrategy(strategy);
            }

            // DbAdapter
            dataNode.setAdapter(adapterFactory.createAdapter(nodeDescriptor, dataSource));

            // DataMaps
            for (String dataMapName : nodeDescriptor.getDataMapNames()) {
                dataNode.addDataMap(dataDomain.getDataMap(dataMapName));
            }

            dataDomain.addNode(dataNode);
        }

        return dataDomain;
    }
}
