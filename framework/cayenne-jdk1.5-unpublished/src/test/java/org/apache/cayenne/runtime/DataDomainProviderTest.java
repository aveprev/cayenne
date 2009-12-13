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
package org.apache.cayenne.runtime;

import java.util.Collection;
import java.util.Collections;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.cayenne.ConfigurationException;
import org.apache.cayenne.DataChannel;
import org.apache.cayenne.access.DataDomain;
import org.apache.cayenne.access.DataNode;
import org.apache.cayenne.access.dbsync.SchemaUpdateStrategy;
import org.apache.cayenne.access.dbsync.SkipSchemaUpdateStrategy;
import org.apache.cayenne.access.dbsync.ThrowOnPartialOrCreateSchemaStrategy;
import org.apache.cayenne.configuration.AdhocObjectFactory;
import org.apache.cayenne.configuration.DataChannelDescriptor;
import org.apache.cayenne.configuration.DataChannelDescriptorLoader;
import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.configuration.DataSourceFactoryLoader;
import org.apache.cayenne.configuration.DbAdapterFactory;
import org.apache.cayenne.configuration.DefaultAdhocObjectFactory;
import org.apache.cayenne.configuration.DefaultConfigurationNameMapper;
import org.apache.cayenne.configuration.DefaultRuntimeProperties;
import org.apache.cayenne.configuration.ConfigurationNameMapper;
import org.apache.cayenne.configuration.RuntimeProperties;
import org.apache.cayenne.configuration.mock.MockDataSourceFactory;
import org.apache.cayenne.configuration.mock.MockDataSourceFactoryLoader;
import org.apache.cayenne.dba.DbAdapter;
import org.apache.cayenne.dba.MockDbAdapter;
import org.apache.cayenne.dba.oracle.OracleAdapter;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.resource.Resource;
import org.apache.cayenne.resource.ResourceLocator;
import org.apache.cayenne.resource.mock.MockResource;

public class DataDomainProviderTest extends TestCase {

    public void testGet() {

        // create dependencies
        final String testConfigName = "testConfig";
        final DataChannelDescriptor testDescriptor = new DataChannelDescriptor();

        DataMap map1 = new DataMap("map1");
        testDescriptor.getDataMaps().add(map1);

        DataMap map2 = new DataMap("map2");
        testDescriptor.getDataMaps().add(map2);

        DataNodeDescriptor nodeDescriptor1 = new DataNodeDescriptor();
        nodeDescriptor1.setName("node1");
        nodeDescriptor1.getDataMapNames().add("map1");
        nodeDescriptor1.setAdapterType(OracleAdapter.class.getName());
        nodeDescriptor1.setDataSourceFactoryType(MockDataSourceFactory.class.getName());
        nodeDescriptor1.setLocation("jdbc/testDataNode1");
        nodeDescriptor1
                .setSchemaUpdateStrategyType(ThrowOnPartialOrCreateSchemaStrategy.class
                        .getName());
        testDescriptor.getDataNodeDescriptors().add(nodeDescriptor1);

        DataNodeDescriptor nodeDescriptor2 = new DataNodeDescriptor();
        nodeDescriptor2.setName("node2");
        nodeDescriptor2.getDataMapNames().add("map2");
        nodeDescriptor2.setLocation("testDataNode2.driver.xml");
        testDescriptor.getDataNodeDescriptors().add(nodeDescriptor2);

        final ResourceLocator locator = new ResourceLocator() {

            public Collection<Resource> findResources(String name) {
                assertEquals("cayenne-" + testConfigName + ".xml", name);
                return Collections.<Resource> singleton(new MockResource());
            }
        };

        final DataChannelDescriptorLoader testLoader = new DataChannelDescriptorLoader() {

            public DataChannelDescriptor load(Resource configurationResource)
                    throws ConfigurationException {
                return testDescriptor;
            }
        };

        final DefaultRuntimeProperties testProperties = new DefaultRuntimeProperties(
                Collections.singletonMap(
                        RuntimeProperties.CAYENNE_RUNTIME_NAME,
                        testConfigName));

        Module testModule = new Module() {

            public void configure(Binder binder) {
                binder.bind(ResourceLocator.class).toInstance(locator);
                binder.bind(RuntimeProperties.class).toInstance(testProperties);
                binder.bind(ConfigurationNameMapper.class).to(DefaultConfigurationNameMapper.class);
                binder.bind(DataChannelDescriptorLoader.class).toInstance(testLoader);
                binder.bind(SchemaUpdateStrategy.class).toInstance(
                        new SkipSchemaUpdateStrategy());
                binder.bind(DbAdapterFactory.class).toInstance(new DbAdapterFactory() {

                    public DbAdapter createAdapter(
                            DataNodeDescriptor nodeDescriptor,
                            DataSource dataSource) throws Exception {

                        if (nodeDescriptor.getAdapterType() != null) {
                            return (DbAdapter) Class.forName(
                                    nodeDescriptor.getAdapterType()).newInstance();
                        }

                        return new MockDbAdapter();
                    }
                });

                binder.bind(DataSourceFactoryLoader.class).toInstance(
                        new MockDataSourceFactoryLoader());
                binder.bind(AdhocObjectFactory.class).to(DefaultAdhocObjectFactory.class);
            }
        };

        Injector injector = DIBootstrap.createInjector(testModule);

        // create and initialize provide instance to test
        DataDomainProvider provider = new DataDomainProvider();
        injector.injectMembers(provider);

        DataChannel channel = provider.get();
        assertNotNull(channel);

        assertTrue(channel instanceof DataDomain);

        assertSame(
                "DataDomainProvider must return the same instance of DataChannel on subsequent calls",
                channel,
                provider.get());

        DataDomain domain = (DataDomain) channel;
        assertEquals(2, domain.getDataMaps().size());
        assertTrue(domain.getDataMaps().contains(map1));
        assertTrue(domain.getDataMaps().contains(map2));

        assertEquals(2, domain.getDataNodes().size());
        DataNode node1 = domain.getNode("node1");
        assertNotNull(node1);
        assertEquals(1, node1.getDataMaps().size());
        assertSame(map1, node1.getDataMaps().iterator().next());
        assertSame(node1, domain.lookupDataNode(map1));
        assertEquals(nodeDescriptor1.getDataSourceFactoryType(), node1
                .getDataSourceFactory());
        assertNotNull(node1.getDataSource());
        assertEquals(nodeDescriptor1.getLocation(), node1.getDataSourceLocation());

        assertEquals(nodeDescriptor1.getSchemaUpdateStrategyType(), node1
                .getSchemaUpdateStrategyName());
        assertNotNull(node1.getSchemaUpdateStrategy());
        assertEquals(nodeDescriptor1.getSchemaUpdateStrategyType(), node1
                .getSchemaUpdateStrategy()
                .getClass()
                .getName());

        assertNotNull(node1.getAdapter());
        assertEquals(OracleAdapter.class, node1.getAdapter().getClass());

        DataNode node2 = domain.getNode("node2");
        assertNotNull(node2);
        assertEquals(1, node2.getDataMaps().size());
        assertSame(map2, node2.getDataMaps().iterator().next());
        assertSame(node2, domain.lookupDataNode(map2));
        assertNull(node2.getDataSourceFactory());
        assertNotNull(node2.getDataSource());
        assertEquals(nodeDescriptor2.getLocation(), node2.getDataSourceLocation());
        assertEquals(SkipSchemaUpdateStrategy.class.getName(), node2
                .getSchemaUpdateStrategyName());
        assertNotNull(node2.getSchemaUpdateStrategy());
        assertEquals(SkipSchemaUpdateStrategy.class.getName(), node2
                .getSchemaUpdateStrategy()
                .getClass()
                .getName());

        assertNotNull(node2.getAdapter());
        assertEquals(MockDbAdapter.class, node2.getAdapter().getClass());
    }
}
