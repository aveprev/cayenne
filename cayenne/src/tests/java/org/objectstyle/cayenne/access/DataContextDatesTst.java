/* ====================================================================
 *
 * The ObjectStyle Group Software License, Version 1.0
 *
 * Copyright (c) 2002-2004 The ObjectStyle Group
 * and individual authors of the software.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        ObjectStyle Group (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "ObjectStyle Group" and "Cayenne"
 *    must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact andrus@objectstyle.org.
 *
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    nor may "ObjectStyle" appear in their names without prior written
 *    permission of the ObjectStyle Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE OBJECTSTYLE GROUP OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the ObjectStyle Group.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 *
 */

package org.objectstyle.cayenne.access;

import java.util.Calendar;
import java.util.Date;

import org.objectstyle.art.DateTest;
import org.objectstyle.cayenne.query.SelectQuery;
import org.objectstyle.cayenne.unittest.CayenneTestCase;

/**
 * Tests Date handling in Cayenne.
 * 
 * @author Andrei Adamchik
 */
public class DataContextDatesTst extends CayenneTestCase {

    protected DataContext ctxt;

    public void testDate() throws Exception {
        DateTest test = (DateTest) ctxt.createAndRegisterNewObject("DateTest");

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2002, 1, 1);
        Date nowDate = cal.getTime();
        test.setDateColumn(nowDate);
        ctxt.commitChanges();

        SelectQuery q = new SelectQuery(DateTest.class);
        DateTest testRead = (DateTest) ctxt.performQuery(q).get(0);
        assertNotNull(testRead.getDateColumn());
        assertEquals(nowDate, testRead.getDateColumn());
    }

    public void testTime() throws Exception {
        DateTest test = (DateTest) ctxt.createAndRegisterNewObject("DateTest");

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(1970, 0, 1, 1, 20, 30);
        Date nowTime = cal.getTime();
        test.setTimeColumn(nowTime);
        ctxt.commitChanges();

        SelectQuery q = new SelectQuery(DateTest.class);
        DateTest testRead = (DateTest) ctxt.performQuery(q).get(0);
        assertNotNull(testRead.getTimeColumn());
        assertEquals(nowTime, testRead.getTimeColumn());
    }

    public void testTimestamp() throws Exception {
        DateTest test = (DateTest) ctxt.createAndRegisterNewObject("DateTest");

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2003, 1, 1, 1, 20, 30);
        Date now = cal.getTime();
        test.setTimestampColumn(now);
        ctxt.commitChanges();

        SelectQuery q = new SelectQuery(DateTest.class);
        DateTest testRead = (DateTest) ctxt.performQuery(q).get(0);
        assertNotNull(testRead.getTimestampColumn());
        assertEquals(now, testRead.getTimestampColumn());
    }

    protected void setUp() throws Exception {
        getDatabaseSetup().cleanTableData();
        ctxt = createDataContext();
    }
}
