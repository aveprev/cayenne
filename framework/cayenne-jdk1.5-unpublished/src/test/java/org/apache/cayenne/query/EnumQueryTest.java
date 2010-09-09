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
package org.apache.cayenne.query;

import static java.util.Collections.singletonMap;

import java.util.List;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.test.jdbc.DBHelper;
import org.apache.cayenne.test.jdbc.TableHelper;
import org.apache.cayenne.testdo.enumtest.User;
import org.apache.cayenne.testdo.enumtest.UserType;
import org.apache.cayenne.unit.di.server.ServerCase;
import org.apache.cayenne.unit.di.server.UseServerRuntime;

@UseServerRuntime("cayenne-enum.xml")
public class EnumQueryTest extends ServerCase {

    @Inject
    protected ObjectContext context;

    @Inject
    protected DBHelper dbHelper;

    @Override
    protected void setUpAfterInjection() throws Exception {
        dbHelper.deleteAll("USER");
    }

    private void createUserDataSet() throws Exception {
        TableHelper tableHelper = new TableHelper(dbHelper, "USER");
        tableHelper.setColumns("ID", "NAME", "TYPE");
        tableHelper.insert(1, "user1", 0);
        tableHelper.insert(2, "user2", 1);
    }

    public void testSelectByEnumValue() throws Exception {
        createUserDataSet();

        Expression expr = Expression.fromString("type = $type");
        SelectQuery query = new SelectQuery(User.class, expr)
                .queryWithParameters(singletonMap("type", UserType.ORDINARY));
        List users = context.performQuery(query);
        assertEquals(1, users.size());
        assertEquals("user1", ((User)users.get(0)).getName());
    }
    
    public void testSelectByEnumValueSpecifiedAsConstant() throws Exception {
        createUserDataSet();
        
        Expression expr = Expression.fromString("type = org.apache.cayenne.testdo.enumtest.UserType.ADMIN");
        SelectQuery query = new SelectQuery(User.class, expr);
        List users = context.performQuery(query);
        assertEquals(1, users.size());
        assertEquals("user2", ((User)users.get(0)).getName());
    }
}
