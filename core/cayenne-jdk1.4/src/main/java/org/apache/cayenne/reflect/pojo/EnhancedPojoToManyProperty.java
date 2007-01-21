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
package org.apache.cayenne.reflect.pojo;

import org.apache.cayenne.ValueHolder;
import org.apache.cayenne.reflect.Accessor;
import org.apache.cayenne.reflect.ClassDescriptor;
import org.apache.cayenne.reflect.ListProperty;
import org.apache.cayenne.reflect.PropertyException;

/**
 * @since 3.0
 * @author Andrus Adamchik
 */
class EnhancedPojoToManyProperty extends ListProperty {

    private EnhancedPojoPropertyFaultHandler faultHandler;

    EnhancedPojoToManyProperty(ClassDescriptor owner, ClassDescriptor targetDescriptor,
            Accessor accessor, String reverseName) {
        super(owner, targetDescriptor, accessor, reverseName);
        this.faultHandler = new EnhancedPojoPropertyFaultHandler(
                owner.getObjectClass(),
                getName());
    }
    
    protected ValueHolder createCollectionValueHolder(Object object) throws PropertyException {
        ValueHolder holder = super.createCollectionValueHolder(object);
        faultHandler.setFaultProperty(object, false);
        return holder;
    }

    public void invalidate(Object object) {
        faultHandler.setFaultProperty(object, true);
    }
    
    public boolean isFault(Object object) {
        return faultHandler.isFaultProperty(object);
    }
}
