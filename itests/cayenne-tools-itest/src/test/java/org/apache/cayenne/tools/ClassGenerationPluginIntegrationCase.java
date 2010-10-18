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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.cayenne.test.file.FileUtil;
import org.apache.cayenne.test.resource.ResourceUtil;

public abstract class ClassGenerationPluginIntegrationCase extends TestCase {

    protected File testDir;
    protected File mapDir;
    protected File map;
    protected File mapEmbeddables;
    protected File template;

    @Override
    public void setUp() {
        testDir = new File(FileUtil.baseTestDirectory(), String.valueOf(Math
                .abs(new Random().nextInt())));
        assertTrue(testDir.mkdir());
        mapDir = new File(testDir, "map");
        assertTrue(mapDir.mkdir());

        map = new File(testDir, "antmap.xml");
        mapEmbeddables = new File(testDir, "antmap-embeddables.xml");
        template = new File(testDir, "velotemplate.vm");

        ResourceUtil.copyResourceToFile("testmap.map.xml", map);
        ResourceUtil.copyResourceToFile("embeddable.map.xml", mapEmbeddables);
        ResourceUtil.copyResourceToFile(
                "org/apache/cayenne/tools/velotemplate.vm",
                template);
    }

    protected abstract void execute() throws Exception;

    protected abstract void setProperty(String name, Object value);

    /**
     * Test single classes with a non-standard template.
     */
    public void testSingleClassesCustTemplate() throws Exception {
        setProperty("destDir", mapDir);
        setProperty("map", map);
        setProperty("makePairs", false);
        setProperty("usePkgPath", true);
        setProperty("template", template.getPath());

        execute();

        assertContents(
                "org/apache/cayenne/testdo/testmap/Artist.java",
                "Artist",
                "org.apache.cayenne.testdo.testmap",
                "CayenneDataObject");
        assertExists("org/apache/cayenne/testdo/testmap/_Artist.java");
    }

    /** Test single classes generation including full package path. */
    public void testSingleClasses1() throws Exception {
        setProperty("destDir", mapDir);
        setProperty("map", map);
        setProperty("makePairs", false);
        setProperty("usePkgPath", true);

        execute();

        assertContents(
                "org/apache/cayenne/testdo/testmap/Artist.java",
                "Artist",
                "org.apache.cayenne.testdo.testmap",
                "CayenneDataObject");
        assertExists("org/apache/cayenne/testdo/testmap/_Artist.java");
    }

    /** Test single classes generation ignoring package path. */
    public void testSingleClasses2() throws Exception {
        setProperty("destDir", mapDir);
        setProperty("map", map);
        setProperty("makePairs", false);
        setProperty("usePkgPath", false);

        execute();

        assertContents(
                "Artist.java",
                "Artist",
                "org.apache.cayenne.testdo.testmap",
                "CayenneDataObject");
        assertExists("_Artist.java");
        assertExists("org/apache/cayenne/testdo/testmap/Artist.java");
    }

    /** Test pairs generation including full package path. */
    public void testPairs1() throws Exception {
        setProperty("destDir", mapDir);
        setProperty("map", map);
        setProperty("makePairs", true);
        setProperty("usePkgPath", true);

        execute();

        assertContents(
                "org/apache/cayenne/testdo/testmap/Artist.java",
                "Artist",
                "org.apache.cayenne.testdo.testmap",
                "_Artist");
        assertContents(
                "org/apache/cayenne/testdo/testmap/_Artist.java",
                "_Artist",
                "org.apache.cayenne.testdo.testmap",
                "CayenneDataObject");
    }

    /** Test pairs generation in the same directory. */
    public void testPairs2() throws Exception {
        setProperty("destDir", mapDir);
        setProperty("map", map);
        setProperty("makePairs", true);
        setProperty("usePkgPath", false);

        execute();

        assertContents(
                "Artist.java",
                "Artist",
                "org.apache.cayenne.testdo.testmap",
                "_Artist");
        assertContents(
                "_Artist.java",
                "_Artist",
                "org.apache.cayenne.testdo.testmap",
                "CayenneDataObject");
        assertExists("org/apache/cayenne/testdo/testmap/Artist.java");
    }

    /**
     * Test pairs generation including full package path with superclass and subclass in
     * different packages.
     */
    public void testPairs3() throws Exception {
        setProperty("destDir", mapDir);
        setProperty("map", map);
        setProperty("makePairs", true);
        setProperty("usePkgPath", true);
        setProperty("superPkg", "org.apache.cayenne.testdo.testmap.superart");

        execute();

        assertContents(
                "org/apache/cayenne/testdo/testmap/Artist.java",
                "Artist",
                "org.apache.cayenne.testdo.testmap",
                "_Artist");
        assertContents(
                "org/apache/cayenne/testdo/testmap/superart/_Artist.java",
                "_Artist",
                "org.apache.cayenne.testdo.testmap.superart",
                "CayenneDataObject");
    }

    public void testPairsEmbeddable3() throws Exception {
        setProperty("destDir", mapDir);
        setProperty("map", mapEmbeddables);
        setProperty("makePairs", true);
        setProperty("usePkgPath", true);
        setProperty("superPkg", "org.apache.cayenne.testdo.embeddable.auto");

        execute();

        assertContents(
                "org/apache/cayenne/testdo/embeddable/EmbedEntity1.java",
                "EmbedEntity1",
                "org.apache.cayenne.testdo.embeddable",
                "_EmbedEntity1");
        assertContents(
                "org/apache/cayenne/testdo/embeddable/auto/_EmbedEntity1.java",
                "_EmbedEntity1",
                "org.apache.cayenne.testdo.embeddable.auto",
                "CayenneDataObject");
        assertContents(
                "org/apache/cayenne/testdo/embeddable/Embeddable1.java",
                "Embeddable1",
                "org.apache.cayenne.testdo.embeddable",
                "_Embeddable1");
        assertContents(
                "org/apache/cayenne/testdo/embeddable/auto/_Embeddable1.java",
                "_Embeddable1",
                "org.apache.cayenne.testdo.embeddable.auto",
                "Object");
    }

    private String convertPath(String unixPath) {
        return unixPath.replace('/', File.separatorChar);
    }

    private void assertContents(
            String filePath,
            String className,
            String packageName,
            String extendsName) throws Exception {
        File f = new File(mapDir, convertPath(filePath));
        assertTrue(f.isFile());
        assertContents(f, className, packageName, extendsName);
    }

    private void assertExists(String filePath) {
        File f = new File(mapDir, convertPath(filePath));
        assertFalse(f.exists());
    }

    private void assertContents(
            File f,
            String className,
            String packageName,
            String extendsName) throws Exception {

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(
                f)));

        try {
            assertPackage(in, packageName);
            assertClass(in, className, extendsName);
        }
        finally {
            in.close();
        }

    }

    private void assertPackage(BufferedReader in, String packageName) throws Exception {

        String s = null;
        while ((s = in.readLine()) != null) {
            if (Pattern.matches("^package\\s+([^\\s;]+);", s)) {
                assertTrue(s.indexOf(packageName) > 0);
                return;
            }
        }

        fail("No package declaration found.");
    }

    private void assertClass(BufferedReader in, String className, String extendsName)
            throws Exception {

        Pattern classPattern = Pattern.compile("^public\\s+");

        String s = null;
        while ((s = in.readLine()) != null) {
            if (classPattern.matcher(s).find()) {
                assertTrue(s.indexOf(className) > 0);
                assertTrue(s.indexOf(extendsName) > 0);
                assertTrue(s.indexOf(className) < s.indexOf(extendsName));
                return;
            }
        }

        fail("No class declaration found.");
    }
}
