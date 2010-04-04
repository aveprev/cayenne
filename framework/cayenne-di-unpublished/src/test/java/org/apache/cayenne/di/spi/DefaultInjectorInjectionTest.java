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
package org.apache.cayenne.di.spi;

import junit.framework.TestCase;

import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.di.mock.MockImplementation1;
import org.apache.cayenne.di.mock.MockImplementation1_ListConfiguration;
import org.apache.cayenne.di.mock.MockImplementation1_MapConfiguration;
import org.apache.cayenne.di.mock.MockImplementation1_WithInjector;
import org.apache.cayenne.di.mock.MockImplementation2;
import org.apache.cayenne.di.mock.MockImplementation2Sub1;
import org.apache.cayenne.di.mock.MockImplementation2_ConstructorProvider;
import org.apache.cayenne.di.mock.MockImplementation3;
import org.apache.cayenne.di.mock.MockImplementation4;
import org.apache.cayenne.di.mock.MockImplementation5;
import org.apache.cayenne.di.mock.MockInterface1;
import org.apache.cayenne.di.mock.MockInterface2;
import org.apache.cayenne.di.mock.MockInterface3;
import org.apache.cayenne.di.mock.MockInterface4;
import org.apache.cayenne.di.mock.MockInterface5;

public class DefaultInjectorInjectionTest extends TestCase {

    public void testFieldInjection() {

        Module module = new Module() {

            public void configure(Binder binder) {
                binder.bind(MockInterface1.class).to(MockImplementation1.class);
                binder.bind(MockInterface2.class).to(MockImplementation2.class);
            }
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface2 service = injector.getInstance(MockInterface2.class);
        assertNotNull(service);
        assertEquals("altered_MyName", service.getAlteredName());
    }

    public void testFieldInjectionSuperclass() {

        Module module = new Module() {

            public void configure(Binder binder) {
                binder.bind(MockInterface1.class).to(MockImplementation1.class);
                binder.bind(MockInterface2.class).to(MockImplementation2Sub1.class);
                binder.bind(MockInterface3.class).to(MockImplementation3.class);
            }
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface2 service = injector.getInstance(MockInterface2.class);
        assertNotNull(service);
        assertEquals("altered_MyName:XName", service.getAlteredName());
    }

    public void testConstructorInjection() {

        Module module = new Module() {

            public void configure(Binder binder) {
                binder.bind(MockInterface1.class).to(MockImplementation1.class);
                binder.bind(MockInterface4.class).to(MockImplementation4.class);
            }
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface4 service = injector.getInstance(MockInterface4.class);
        assertNotNull(service);
        assertEquals("constructor_MyName", service.getName());
    }

    public void testProviderInjection_Constructor() {

        Module module = new Module() {

            public void configure(Binder binder) {
                binder.bind(MockInterface1.class).to(MockImplementation1.class);
                binder.bind(MockInterface2.class).to(
                        MockImplementation2_ConstructorProvider.class);
            }
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface2 service = injector.getInstance(MockInterface2.class);
        assertEquals("altered_MyName", service.getAlteredName());
    }

    public void testMapInjection() {
        Module module = new Module() {

            public void configure(Binder binder) {
                binder.bind(MockInterface1.class).to(
                        MockImplementation1_MapConfiguration.class);

                binder
                        .bindMap(MockInterface1.class)
                        .put("x", "xvalue")
                        .put("y", "yvalue")
                        .put("x", "xvalue1");
            }
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals(";x=xvalue1;y=yvalue", service.getName());
    }

    public void testListInjection_addValue() {
        Module module = new Module() {

            public void configure(Binder binder) {
                binder.bind(MockInterface1.class).to(
                        MockImplementation1_ListConfiguration.class);

                binder.bindList(MockInterface1.class).add("xvalue").add("yvalue");
            }
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals(";xvalue;yvalue", service.getName());
    }

    public void testListInjection_addType() {
        Module module = new Module() {

            public void configure(Binder binder) {
                binder.bind(MockInterface5.class).to(MockImplementation5.class);

                binder.bind(MockInterface1.class).to(
                        MockImplementation1_ListConfiguration.class);

                binder.bindList(MockInterface1.class).add(MockInterface5.class).add(
                        "yvalue");
            }
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals(";xyz;yvalue", service.getName());
    }

    public void testInjectorInjection() {
        Module module = new Module() {

            public void configure(Binder binder) {
                binder.bind(MockInterface1.class).to(
                        MockImplementation1_WithInjector.class);
            }
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals("injector_not_null", service.getName());
    }

}