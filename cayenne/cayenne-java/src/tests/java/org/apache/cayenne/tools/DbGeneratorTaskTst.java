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

import org.apache.cayenne.dba.sqlserver.SQLServerAdapter;
import org.apache.cayenne.unit.CayenneTestCase;

/**
 * @author Andrei Adamchik
 */
public class DbGeneratorTaskTst extends CayenneTestCase {

    public void testSetUserName() throws Exception {
        DbGeneratorTask task = new DbGeneratorTask();
        task.setUserName("abc");
        assertEquals("abc", task.userName);
    }

    public void testSetPassword() throws Exception {
        DbGeneratorTask task = new DbGeneratorTask();
        task.setPassword("xyz");
        assertEquals("xyz", task.password);
    }

    public void testSetAdapter() throws Exception {
        DbGeneratorTask task = new DbGeneratorTask();
        task.setAdapter(SQLServerAdapter.class.getName());
        assertTrue(task.adapter instanceof SQLServerAdapter);
    }

    public void testSetUrl() throws Exception {
        DbGeneratorTask task = new DbGeneratorTask();
        task.setUrl("jdbc:///");
        assertEquals("jdbc:///", task.url);
    }
}
