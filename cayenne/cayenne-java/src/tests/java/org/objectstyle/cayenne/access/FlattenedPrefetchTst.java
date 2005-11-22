/* ====================================================================
 *
 * The ObjectStyle Group Software License, version 1.1
 * ObjectStyle Group - http://objectstyle.org/
 * 
 * Copyright (c) 2002-2005, Andrei (Andrus) Adamchik and individual authors
 * of the software. All rights reserved.
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
 * 3. The end-user documentation included with the redistribution, if any,
 *    must include the following acknowlegement:
 *    "This product includes software developed by independent contributors
 *    and hosted on ObjectStyle Group web site (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The names "ObjectStyle Group" and "Cayenne" must not be used to endorse
 *    or promote products derived from this software without prior written
 *    permission. For written permission, email
 *    "andrus at objectstyle dot org".
 * 
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    or "Cayenne", nor may "ObjectStyle" or "Cayenne" appear in their
 *    names without prior written permission.
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
 * individuals and hosted on ObjectStyle Group web site.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 */
package org.objectstyle.cayenne.access;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.objectstyle.art.ArtGroup;
import org.objectstyle.art.Artist;
import org.objectstyle.art.Painting;
import org.objectstyle.cayenne.PersistenceState;
import org.objectstyle.cayenne.query.PrefetchTreeNode;
import org.objectstyle.cayenne.query.SelectQuery;
import org.objectstyle.cayenne.unit.CayenneTestCase;

/**
 * @author Andrei Adamchik
 */
public class FlattenedPrefetchTst extends CayenneTestCase {

    protected void setUp() throws Exception {
        super.setUp();

        deleteTestData();
    }

    public void testManyToMany() throws Exception {

        createTestData("testPrefetch1");

        SelectQuery q = new SelectQuery(Artist.class);
        q.addPrefetch(Artist.GROUP_ARRAY_PROPERTY);

        DataContext context = createDataContext();

        List objects = context.performQuery(q);

        // block further queries
        context.setDelegate(new QueryBlockingDelegate());
        assertEquals(3, objects.size());

        Iterator it = objects.iterator();
        while (it.hasNext()) {
            Artist a = (Artist) it.next();
            ToManyList list = (ToManyList) a.getGroupArray();

            assertNotNull(list);
            assertFalse("artist's groups not resolved: " + a, list.needsFetch());
            assertTrue(list.size() > 0);

            Iterator children = list.iterator();
            while (children.hasNext()) {
                ArtGroup g = (ArtGroup) children.next();
                assertEquals(PersistenceState.COMMITTED, g.getPersistenceState());
            }

            // assert no duplicates
            Set s = new HashSet(list);
            assertEquals(s.size(), list.size());
        }

    }

    public void testMultiPrefetch() throws Exception {
        createTestData("testPrefetch2");

        SelectQuery q = new SelectQuery(Painting.class);
        q.addPrefetch(Painting.TO_ARTIST_PROPERTY);
        q.addPrefetch(Painting.TO_ARTIST_PROPERTY + '.' + Artist.GROUP_ARRAY_PROPERTY);

        DataContext context = createDataContext();

        List objects = context.performQuery(q);

        // block further queries
        context.setDelegate(new QueryBlockingDelegate());
        assertEquals(3, objects.size());

        Iterator it = objects.iterator();
        while (it.hasNext()) {
            Painting p = (Painting) it.next();
            Artist a = p.getToArtist();
            assertEquals(PersistenceState.COMMITTED, a.getPersistenceState());

            ToManyList list = (ToManyList) a.getGroupArray();
            assertNotNull(list);
            assertFalse("artist's groups not resolved: " + a, list.needsFetch());
            assertTrue(list.size() > 0);

            Iterator children = list.iterator();
            while (children.hasNext()) {
                ArtGroup g = (ArtGroup) children.next();
                assertEquals(PersistenceState.COMMITTED, g.getPersistenceState());
            }

            // assert no duplicates
            Set s = new HashSet(list);
            assertEquals(s.size(), list.size());
        }
    }

    public void testJointManyToMany() throws Exception {
        createTestData("testPrefetch1");

        SelectQuery q = new SelectQuery(Artist.class);
        q.addPrefetch(Artist.GROUP_ARRAY_PROPERTY).setSemantics(
                PrefetchTreeNode.JOINT_PREFETCH_SEMANTICS);

        DataContext context = createDataContext();

        List objects = context.performQuery(q);
        // block further queries
        context.setDelegate(new QueryBlockingDelegate());

        assertEquals(3, objects.size());

        Iterator it = objects.iterator();
        while (it.hasNext()) {
            Artist a = (Artist) it.next();
            ToManyList list = (ToManyList) a.getGroupArray();

            assertNotNull(list);
            assertFalse("artist's groups not resolved: " + a, list.needsFetch());
            assertTrue(list.size() > 0);

            Iterator children = list.iterator();
            while (children.hasNext()) {
                ArtGroup g = (ArtGroup) children.next();
                assertEquals(PersistenceState.COMMITTED, g.getPersistenceState());
            }

            // assert no duplicates
            Set s = new HashSet(list);
            assertEquals(s.size(), list.size());
        }
    }

    public void testJointMultiPrefetch() throws Exception {
        createTestData("testPrefetch2");

        SelectQuery q = new SelectQuery(Painting.class);
        q.addPrefetch(Painting.TO_ARTIST_PROPERTY).setSemantics(
                PrefetchTreeNode.JOINT_PREFETCH_SEMANTICS);
        q
                .addPrefetch(
                        Painting.TO_ARTIST_PROPERTY + '.' + Artist.GROUP_ARRAY_PROPERTY)
                .setSemantics(PrefetchTreeNode.JOINT_PREFETCH_SEMANTICS);

        DataContext context = createDataContext();

        List objects = context.performQuery(q);

        // block further queries

        context.setDelegate(new QueryBlockingDelegate());
        assertEquals(3, objects.size());

        Iterator it = objects.iterator();
        while (it.hasNext()) {
            Painting p = (Painting) it.next();
            Artist a = p.getToArtist();
            assertEquals(PersistenceState.COMMITTED, a.getPersistenceState());

            ToManyList list = (ToManyList) a.getGroupArray();
            assertNotNull(list);
            assertFalse("artist's groups not resolved: " + a, list.needsFetch());
            assertTrue(list.size() > 0);

            Iterator children = list.iterator();
            while (children.hasNext()) {
                ArtGroup g = (ArtGroup) children.next();
                assertEquals(PersistenceState.COMMITTED, g.getPersistenceState());
            }

            // assert no duplicates

            Set s = new HashSet(list);
            assertEquals(s.size(), list.size());
        }
    }
}