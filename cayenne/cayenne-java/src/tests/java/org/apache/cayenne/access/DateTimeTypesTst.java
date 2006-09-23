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


package org.apache.cayenne.access;

import java.util.Calendar;
import java.util.Date;

import org.apache.art.DateTest;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.unit.CayenneTestCase;

/**
 * Tests Date handling in Cayenne.
 * 
 * @author Andrei Adamchik
 */
public class DateTimeTypesTst extends CayenneTestCase {

    protected DataContext context;

    protected void setUp() throws Exception {
        super.setUp();

        deleteTestData();
        context = createDataContext();
    }

    public void testDate() throws Exception {
        DateTest test = (DateTest) context.createAndRegisterNewObject("DateTest");

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2002, 1, 1);
        Date nowDate = cal.getTime();
        test.setDateColumn(nowDate);
        context.commitChanges();

        SelectQuery q = new SelectQuery(DateTest.class);
        DateTest testRead = (DateTest) context.performQuery(q).get(0);
        assertNotNull(testRead.getDateColumn());
        assertEquals(nowDate, testRead.getDateColumn());
    }

    public void testTime() throws Exception {
        DateTest test = (DateTest) context.createAndRegisterNewObject("DateTest");

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(1970, 0, 1, 1, 20, 30);
        Date nowTime = cal.getTime();
        test.setTimeColumn(nowTime);
        context.commitChanges();

        SelectQuery q = new SelectQuery(DateTest.class);
        DateTest testRead = (DateTest) context.performQuery(q).get(0);
        assertNotNull(testRead.getTimeColumn());

        // OpenBase fails to store seconds for the time
        // do an approximate match rounding to minutes

        assertTrue(
            Math.abs(nowTime.getTime() - testRead.getTimeColumn().getTime()) < 1000 * 60);
    }

    public void testTimestamp() throws Exception {
        DateTest test = (DateTest) context.createAndRegisterNewObject("DateTest");

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2003, 1, 1, 1, 20, 30);

        // most databases fail millisecond accuracy
        // cal.set(Calendar.MILLISECOND, 55);

        Date now = cal.getTime();
        test.setTimestampColumn(now);
        context.commitChanges();

        SelectQuery q = new SelectQuery(DateTest.class);
        DateTest testRead = (DateTest) context.performQuery(q).get(0);
        assertNotNull(testRead.getTimestampColumn());
        assertEquals(now, testRead.getTimestampColumn());
    }
}
