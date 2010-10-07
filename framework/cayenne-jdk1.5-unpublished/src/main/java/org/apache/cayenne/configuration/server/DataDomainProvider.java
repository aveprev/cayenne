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

    @Inject
    protected DataDomainNameResolver domainNameResolver;

    @Inject
    protected DataDomainConfigurationResolver domainConfigurationResolver;

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

    protected DataDomain createAndInitDataDomain() throws Exception {
        DataDomain domain = createDataDomain(domainNameResolver.resolveDomainName());
        Collection<Resource> configurations = domainConfigurationResolver
                .resolveConfigurations();
        return initDataDomain(domain, configurations);
    }

    private DataDomain initDataDomain(
            DataDomain domain,
            Collection<Resource> configurations) throws Exception {
        domain.setEntitySorter(injector.getInstance(EntitySorter.class));
        domain.setEventManager(injector.getInstance(EventManager.class));
        for (Resource configuration : configurations) {
            DataChannelDescriptor descriptor = getDataChannelDescriptor(configuration);
            domain.initWithProperties(descriptor.getProperties());
            initDataDomainWithDataMaps(domain, descriptor.getDataMaps());
            initDataDomainWithDataNodes(domain, descriptor.getNodeDescriptors());
        }
        return domain;
    }

    private DataChannelDescriptor getDataChannelDescriptor(Resource configuration) {
        ConfigurationTree<DataChannelDescriptor> tree = loader.load(configuration);
        if (!tree.getLoadFailures().isEmpty()) {
            throw new DataDomainLoadException(tree, "Error loading DataChannelDescriptor");
        }
        return tree.getRootNode();
    }

    private void initDataDomainWithDataMaps(
            DataDomain domain,
            Collection<DataMap> dataMaps) {
        for (DataMap dataMap : dataMaps) {
            domain.addDataMap(dataMap);
        }
    }

    private void initDataDomainWithDataNodes(
            DataDomain domain,
            Collection<DataNodeDescriptor> dataNodeDescriptors) throws Exception {
        for (DataNodeDescriptor nodeDescriptor : dataNodeDescriptors) {
            domain.addNode(createDataNode(domain, nodeDescriptor));
        }
    }

    private DataNode createDataNode(DataDomain domain, DataNodeDescriptor nodeDescriptor)
            throws Exception {
        DataNode dataNode = new DataNode(nodeDescriptor.getName());
        DataSource dataSource = initDataNodeWithDataSource(nodeDescriptor, dataNode);
        initDataNodeWithSchemeUpdateStrategy(dataNode, nodeDescriptor);
        initDataNodeWithDbAdapter(dataNode, nodeDescriptor, dataSource);
        initDataNodeWithDataMaps(dataNode, nodeDescriptor, domain);
        return dataNode;
    }

    private DataSource initDataNodeWithDataSource(
            DataNodeDescriptor nodeDescriptor,
            DataNode dataNode) throws Exception {
        dataNode.setDataSourceLocation(nodeDescriptor.getParameters());

        DataSourceFactory dataSourceFactory = dataSourceFactoryLoader
                .getDataSourceFactory(nodeDescriptor);

        DataSource dataSource = dataSourceFactory.getDataSource(nodeDescriptor);

        dataNode.setDataSourceFactory(nodeDescriptor.getDataSourceFactoryType());
        dataNode.setDataSource(dataSource);
        return dataSource;
    }

    private void initDataNodeWithSchemeUpdateStrategy(
            DataNode dataNode,
            DataNodeDescriptor nodeDescriptor) {
        String schemaUpdateStrategyType = nodeDescriptor.getSchemaUpdateStrategyType();

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
    }

    private void initDataNodeWithDbAdapter(
            DataNode dataNode,
            DataNodeDescriptor nodeDescriptor,
            DataSource dataSource) throws Exception {
        dataNode.setAdapter(adapterFactory.createAdapter(nodeDescriptor, dataSource));
    }

    private void initDataNodeWithDataMaps(
            DataNode dataNode,
            DataNodeDescriptor nodeDescriptor,
            DataDomain domain) {
        for (String dataMapName : nodeDescriptor.getDataMapNames()) {
            dataNode.addDataMap(domain.getDataMap(dataMapName));
        }
    }
}
