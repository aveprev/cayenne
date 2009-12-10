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

import org.apache.cayenne.access.DataDomain;
import org.apache.cayenne.configuration.CayenneServerModule;
import org.apache.cayenne.di.Module;

/**
 * A runtime representing Cayenne server-stack that connects directly to the database via
 * JDBC. The term "server" is used as opposed to ROP "client". Any application, desktop,
 * server, etc. that has a direct JDBC connection should be using this runtime.
 * 
 * @since 3.1
 */
public class CayenneServerRuntime extends CayenneRuntime {

    public CayenneServerRuntime(String name) {
        super(name, new CayenneServerModule(name));
    }

    public CayenneServerRuntime(String name, Module... modules) {
        super(name, modules);
    }

    /**
     * Returns the main runtime DataDomain. Note that by default the returned DataDomain
     * is the same as the main DataChannel returned by {@link #getDataChannel()}. Although
     * users may redefine DataChannel provider in the DI registry, for instance to
     * decorate this DataDomain with a custom wrapper.
     */
    public DataDomain getDataDomain() {
        return injector.getInstance(DataDomain.class);
    }

}