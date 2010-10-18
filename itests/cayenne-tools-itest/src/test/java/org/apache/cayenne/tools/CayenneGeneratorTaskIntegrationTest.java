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

import java.io.File;

import org.apache.cayenne.reflect.FieldAccessor;
import org.apache.cayenne.test.resource.ResourceUtil;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.Path;

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

    public void testCrossDataMapRelationships() throws Exception {
        File map = new File(testDir, "cgen-dependent.map.xml");
        ResourceUtil.copyResourceToFile("org/apache/cayenne/tools/cgen-dependent.map.xml", map);

        File additionalMaps[] = new File[1];
        additionalMaps[0] = new File(testDir, "cgen.map.xml");
        ResourceUtil.copyResourceToFile("org/apache/cayenne/tools/cgen.map.xml", additionalMaps[0]);

        FileList additionalMapsFilelist = new FileList();
        additionalMapsFilelist.setDir(additionalMaps[0].getParentFile());
        additionalMapsFilelist.setFiles(additionalMaps[0].getName());

        Path additionalMapsPath = new Path(task.getProject());
        additionalMapsPath.addFilelist(additionalMapsFilelist);

        task.setMap(map);
        task.setAdditionalMaps(additionalMapsPath);
        task.setMakepairs(true);
        task.setOverwrite(false);
        task.setMode("entity");
        task.setIncludeEntities("MyArtGroup");
        task.setDestDir(mapDir);
        task.setSuperpkg("org.apache.cayenne.testdo.cgen2.auto");
        task.setUsepkgpath(true);

        execute();

        assertContents("org/apache/cayenne/testdo/cgen2/MyArtGroup.java", "MyArtGroup",
                "org.apache.cayenne.testdo.cgen2", "_MyArtGroup");

        assertContents("org/apache/cayenne/testdo/cgen2/auto/_MyArtGroup.java", "_MyArtGroup",
                "org.apache.cayenne.testdo.cgen2.auto", "CayenneDataObject");
        assertContents("org/apache/cayenne/testdo/cgen2/auto/_MyArtGroup.java",
                "import org.apache.cayenne.testdo.testmap.ArtGroup;");
        assertContents("org/apache/cayenne/testdo/cgen2/auto/_MyArtGroup.java", " ArtGroup getToParentGroup()");
        assertContents("org/apache/cayenne/testdo/cgen2/auto/_MyArtGroup.java",
                "setToParentGroup(ArtGroup toParentGroup)");
    }
}
