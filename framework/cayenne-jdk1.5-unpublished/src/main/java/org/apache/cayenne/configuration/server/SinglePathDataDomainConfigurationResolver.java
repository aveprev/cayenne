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
import java.util.Collections;

import org.apache.cayenne.resource.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SinglePathDataDomainConfigurationResolver extends
        AbstractDataDomainConfigurationResolver {

    private static final Log logger = LogFactory
            .getLog(SinglePathDataDomainConfigurationResolver.class);

    private String configurationLocation;

    public SinglePathDataDomainConfigurationResolver(String configurationLocation) {
        this.configurationLocation = configurationLocation;
    }

    public Collection<Resource> resolveConfigurations() {
        Collection<Resource> configurations = findConfigurationResources(configurationLocation);
        Resource configuration = configurations.iterator().next();
        if (configurations.size() > 1) {
            logger.info("found "
                    + configurations.size()
                    + " configurations, will use the first one: "
                    + configuration.getURL());
        }
        return Collections.singleton(configuration);
    }
}
