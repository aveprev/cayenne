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
package org.apache.cayenne.jpa.map;

import org.apache.cayenne.util.TreeNodeChild;

/**
 */
public class JpaPersistenceUnitDefaults {

    protected String schema;
    protected String catalog;
    protected AccessType access;
    protected boolean cascadePersist;
    protected JpaEntityListeners entityListeners;

    @TreeNodeChild
    public JpaEntityListeners getEntityListeners() {
        return entityListeners;
    }

    public void setEntityListeners(JpaEntityListeners entityListeners) {
        this.entityListeners = entityListeners;
    }

    public AccessType getAccess() {
        return access;
    }

    public void setAccess(AccessType access) {
        this.access = access;
    }

    public boolean isCascadePersist() {
        return cascadePersist;
    }

    public void setCascadePersist(boolean cascadePersist) {
        this.cascadePersist = cascadePersist;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
