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

package org.apache.cayenne.tools;

import org.apache.cayenne.reflect.FieldAccessor;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;

public class CayenneGeneratorTaskIntegrationTest extends ClassGenerationPluginIntegrationCase {

    protected CayenneGeneratorTask task;

    @Override
    public void setUp() {
        super.setUp();

        Project project = new Project();
        project.setBaseDir(testDir);

        task = new CayenneGeneratorTask();
        task.setProject(project);
        task.setTaskName("Test");
        task.setLocation(Location.UNKNOWN_LOCATION);
    }

    @Override
    protected void execute() throws Exception {
        task.execute();
    }

    @Override
    protected void setProperty(String name, Object value) {
        // Some ant plugin properties have other name
        if (!"destDir".equals(name)) {
            name = name.toLowerCase();
        }
        new FieldAccessor(task.getClass(), name, value.getClass()).setValue(task, value);
    }
}
