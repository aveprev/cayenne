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
import java.util.List;

import javax.sql.DataSource;

import org.apache.cayenne.ConfigurationException;
import org.apache.cayenne.DataChannel;
import org.apache.cayenne.DataChannelFilter;
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

/**
 * A {@link DataChannel} provider that provides a single instance of DataDomain configured
 * per configuration supplied via injected {@link DataChannelDescriptorLoader}.
 * 
 * @since 3.1
 */
public class DataDomainProvider implements Provider<DataDomain> {

    /**
     * A DI key for the list storing DataDomain filters.
     */
    public static final String FILTERS_LIST = "org.apache.cayenne.configuration.server.DataDomainProvider.filters";

    @Inject
    protected DataDomainConfigurationResolver configurationResolver;

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

    @Inject(FILTERS_LIST)
    protected List<DataChannelFilter> filters;

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
            throw new DataDomainLoadException(
                    "Error loading DataChannel: '%s'",
                    e,
                    e.getMessage());
        }
    }

    protected DataDomain createDataDomain(String name) {
        return new DataDomain(name);
    }

    protected DataDomain createAndInitDataDomain(DataChannelDescriptor descriptor) {
        DataDomain dataDomain = createDataDomain(descriptor.getName());
        dataDomain.setEntitySorter(injector.getInstance(EntitySorter.class));
        dataDomain.setEventManager(injector.getInstance(EventManager.class));
        dataDomain.initWithProperties(descriptor.getProperties());
        return dataDomain;
    }

    protected DataDomain createAndInitDataDomain() throws Exception {
        Collection<Resource> configurations = configurationResolver
                .resolveConfigurations();
        DataDomain dataDomain = null;
        for (Resource configuration : configurations) {
            ConfigurationTree<DataChannelDescriptor> tree = loader.load(configuration);
            if (!tree.getLoadFailures().isEmpty()) {
                // TODO: andrus 03/10/2010 - log the errors before throwing?
                throw new DataDomainLoadException(
                        tree,
                        "Error loading DataChannelDescriptor");
            }
            DataChannelDescriptor descriptor = tree.getRootNode();

            if (dataDomain == null) {
                // using the first configuration to resolve data domain name
                dataDomain = createAndInitDataDomain(descriptor);
            }
            initDataDomainWithDataMaps(dataDomain, descriptor);
            initDataDomainWithDataNodes(dataDomain, descriptor);
            initDataDomainWithFilters(dataDomain);
        }
        return dataDomain;
    }

    private void initDataDomainWithFilters(DataDomain dataDomain) {
        for (DataChannelFilter filter : filters) {
            filter.init(dataDomain);
        }

        dataDomain.setFilters(filters);
    }

    private void initDataDomainWithDataNodes(
            DataDomain dataDomain,
            DataChannelDescriptor descriptor) throws Exception {
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
    }

    private void initDataDomainWithDataMaps(
            DataDomain dataDomain,
            DataChannelDescriptor descriptor) {
        for (DataMap dataMap : descriptor.getDataMaps()) {
            dataDomain.addDataMap(dataMap);
        }
    }
}
