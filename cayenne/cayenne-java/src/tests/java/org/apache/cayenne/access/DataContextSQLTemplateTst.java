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

import java.util.List;
import java.util.Map;

import org.apache.art.Artist;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.query.SQLTemplate;
import org.apache.cayenne.unit.CayenneTestCase;

/**
 * @author Andrei Adamchik
 */
public class DataContextSQLTemplateTst extends CayenneTestCase {

    protected DataContext context;

    protected void setUp() throws Exception {
        super.setUp();
        deleteTestData();
        context = createDataContext();
    }

    public void testFetchDataRows() throws Exception {
        getAccessStack().createTestData(DataContextTestBase.class, "testArtists", null);

        String template = "SELECT * FROM ARTIST ORDER BY ARTIST_ID";
        SQLTemplate query = new SQLTemplate(Artist.class, template);

        getSQLTemplateBuilder().updateSQLTemplate(query);

        query.setFetchingDataRows(true);

        List rows = context.performQuery(query);
        assertEquals(DataContextTestBase.artistCount, rows.size());
        assertTrue(
                "Expected DataRow, got this: " + rows.get(1),
                rows.get(1) instanceof DataRow);

        DataRow row2 = (DataRow) rows.get(1);
        assertEquals(3, row2.size());
        assertEquals(new Integer(33002), row2.get("ARTIST_ID"));
    }

    public void testFetchObjects() throws Exception {
        getAccessStack().createTestData(DataContextTestBase.class, "testArtists", null);

        String template = "SELECT * FROM ARTIST ORDER BY ARTIST_ID";
        SQLTemplate query = getSQLTemplateBuilder().createSQLTemplate(
                Artist.class,
                template);

        query.setFetchingDataRows(false);

        List objects = context.performQuery(query);
        assertEquals(DataContextTestBase.artistCount, objects.size());
        assertTrue(objects.get(1) instanceof Artist);

        Artist artist2 = (Artist) objects.get(1);
        assertEquals("artist2", artist2.getArtistName());
    }

    public void testFetchLimit() throws Exception {
        getAccessStack().createTestData(DataContextTestBase.class, "testArtists", null);

        int fetchLimit = 3;

        // sanity check
        assertTrue(fetchLimit < DataContextTestBase.artistCount);
        String template = "SELECT * FROM ARTIST ORDER BY ARTIST_ID";
        SQLTemplate query = getSQLTemplateBuilder().createSQLTemplate(
                Artist.class,
                template);
        query.setFetchLimit(fetchLimit);

        List objects = context.performQuery(query);
        assertEquals(fetchLimit, objects.size());
        assertTrue(objects.get(0) instanceof Artist);
    }

    public void testPageSize() throws Exception {
        getAccessStack().createTestData(DataContextTestBase.class, "testArtists", null);

        int pageSize = 3;

        // sanity check
        assertTrue(pageSize < DataContextTestBase.artistCount);

        String template = "SELECT * FROM ARTIST ORDER BY ARTIST_ID";
        SQLTemplate query = getSQLTemplateBuilder().createSQLTemplate(
                Artist.class,
                template);

        query.setPageSize(pageSize);

        List objects = context.performQuery(query);
        assertEquals(DataContextTestBase.artistCount, objects.size());
        assertTrue(objects.get(0) instanceof Artist);

        assertTrue(objects instanceof IncrementalFaultList);
        IncrementalFaultList pagedList = (IncrementalFaultList) objects;
        assertEquals(DataContextTestBase.artistCount - pageSize, pagedList
                .getUnfetchedObjects());

        // check if we can resolve subsequent pages
        Artist artist = (Artist) objects.get(pageSize);

        int expectUnresolved = DataContextTestBase.artistCount - pageSize - pageSize;
        if (expectUnresolved < 0) {
            expectUnresolved = 0;
        }
        assertEquals(expectUnresolved, pagedList.getUnfetchedObjects());
        assertEquals("artist" + (pageSize + 1), artist.getArtistName());
    }

    public void testIteratedQuery() throws Exception {
        getAccessStack().createTestData(DataContextTestBase.class, "testArtists", null);

        String template = "SELECT * FROM ARTIST ORDER BY ARTIST_ID";
        SQLTemplate query = getSQLTemplateBuilder().createSQLTemplate(
                Artist.class,
                template);

        ResultIterator it = context.performIteratedQuery(query);

        try {
            int i = 0;

            while (it.hasNextRow()) {
                i++;

                Map row = it.nextDataRow();
                assertEquals(3, row.size());
                assertEquals(new Integer(33000 + i), row.get("ARTIST_ID"));
            }

            assertEquals(DataContextTestBase.artistCount, i);
        }
        finally {
            it.close();
        }
    }
}
