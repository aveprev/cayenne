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
package org.apache.cayenne.gen;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import org.apache.cayenne.map.*;
import org.apache.cayenne.unit.BasicCase;
import org.apache.cayenne.query.NamedQuery;

public class ClassGenerationActionTest extends BasicCase {

    protected ClassGenerationAction action;
    protected Collection<StringWriter> writers;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.writers = new ArrayList<StringWriter>(3);
        this.action = new ClassGenerationAction() {

            @Override
            protected Writer openWriter(TemplateType templateType) throws Exception {
                StringWriter writer = new StringWriter();
                writers.add(writer);
                return writer;
            }
        };
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        action = null;
        writers = null;
    }

    public void testExecuteArtifactPairsImports() throws Exception {

        ObjEntity testEntity1 = new ObjEntity("TE1");
        testEntity1.setClassName("org.example.TestClass1");

        action.setMakePairs(true);
        action.setSuperPkg("org.example.auto");

        List<String> generated = execute(new EntityArtifact(testEntity1));
        assertNotNull(generated);
        assertEquals(2, generated.size());

        String superclass = generated.get(0);
        assertTrue(superclass, superclass.contains("package org.example.auto;"));
        assertTrue(superclass, superclass.contains("import org.apache.cayenne.CayenneDataObject;"));

        String subclass = generated.get(1);
        assertTrue(subclass, subclass.contains("package org.example;"));
        assertTrue(
                subclass,
                subclass.contains("import org.example.auto._TestClass1;"));
    }

    public void testExecuteArtifactPairsMapRelationships() throws Exception {

        ObjEntity testEntity1 = new ObjEntity("TE1");
        testEntity1.setClassName("org.example.TestClass1");

        final ObjEntity testEntity2 = new ObjEntity("TE1");
        testEntity2.setClassName("org.example.TestClass2");

        ObjRelationship relationship = new ObjRelationship("xMap") {

            @Override
            public boolean isToMany() {
                return true;
            }
            
            @Override
            public Entity getTargetEntity() {
                return testEntity2;
            }
        };
        relationship.setCollectionType("java.util.Map");
        testEntity1.addRelationship(relationship);

        action.setMakePairs(true);

        List<String> generated = execute(new EntityArtifact(testEntity1));
        assertNotNull(generated);
        assertEquals(2, generated.size());

        String superclass = generated.get(0);
        assertTrue(superclass, superclass.contains("import java.util.Map;"));
    }
    
    
    public void testExecuteArtifactPairsAttribute() throws Exception {

        ObjEntity testEntity1 = new ObjEntity("TE1");
        testEntity1.setClassName("org.example.TestClass1");
        
        ObjAttribute attr = new ObjAttribute();
        attr.setName("ID");
        attr.setType("int");

        ObjAttribute attr1 = new ObjAttribute();
        attr1.setName("name");
        attr1.setType("char");
        
        testEntity1.addAttribute(attr);
        testEntity1.addAttribute(attr1);

        action.setMakePairs(true);

        List<String> generated = execute(new EntityArtifact(testEntity1));
        assertNotNull(generated);
        assertEquals(2, generated.size());
        String superclass = generated.get(0);
        
        assertTrue(superclass, superclass.contains("public void setID(int ID)"));
        assertTrue(superclass, superclass.contains("writeProperty(\"ID\", ID);"));
        
        assertTrue(superclass, superclass.contains("public int getID()"));
        assertTrue(superclass, superclass.contains("Object value = readProperty(\"ID\");"));
        assertTrue(superclass, superclass.contains("return (value != null) ? (Integer) value : 0;"));
        
        assertTrue(superclass, superclass.contains("public void setName(char name)"));
        assertTrue(superclass, superclass.contains("writeProperty(\"name\", name);"));
        
        assertTrue(superclass, superclass.contains("public char getName()"));
        assertTrue(superclass, superclass.contains("Object value = readProperty(\"name\");"));
        assertTrue(superclass, superclass.contains("return (value != null) ? (Character) value : 0;"));

    }

    public void testExecuteDataMapQueryNames() throws Exception {
        runDataMapTest(false);
    }

    public void testExecuteClientDataMapQueryNames() throws Exception {
        runDataMapTest(true);
    }

    private void runDataMapTest(boolean client) throws Exception {
        DataMap map = new DataMap();
        map.addQuery(new NamedQuery("TestQuery"));
        map.setName("testmap");
        List<String> generated;
        if (client) {
            map.setDefaultClientPackage("testpackage");
            generated = execute(new ClientDataMapArtifact(map,map.getQueries()));
        } else {
            map.setDefaultPackage("testpackage");
            generated = execute(new DataMapArtifact(map,map.getQueries()));
        }
        assertEquals(2,generated.size());
        assertTrue(generated.get(0).contains("public static final String TEST_QUERY_QUERYNAME = \"TestQuery\""));
    }

    protected List<String> execute(Artifact artifact) throws Exception {

        action.execute(artifact);

        List<String> strings = new ArrayList<String>(writers.size());
        for (StringWriter writer : writers) {
            strings.add(writer.toString());
        }
        return strings;
    }
}
