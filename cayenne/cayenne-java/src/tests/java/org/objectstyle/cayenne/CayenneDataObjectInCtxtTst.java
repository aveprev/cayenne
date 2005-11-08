/*
 * ==================================================================== The ObjectStyle
 * Group Software License, version 1.1 ObjectStyle Group - http://objectstyle.org/
 * Copyright (c) 2002-2005, Andrei (Andrus) Adamchik and individual authors of the
 * software. All rights reserved. Redistribution and use in source and binary forms, with
 * or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. 2. Redistributions in binary form must
 * reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. The end-user documentation included with the redistribution, if any, must include
 * the following acknowlegement: "This product includes software developed by independent
 * contributors and hosted on ObjectStyle Group web site (http://objectstyle.org/)."
 * Alternately, this acknowlegement may appear in the software itself, if and wherever
 * such third-party acknowlegements normally appear. 4. The names "ObjectStyle Group" and
 * "Cayenne" must not be used to endorse or promote products derived from this software
 * without prior written permission. For written permission, email "andrus at objectstyle
 * dot org". 5. Products derived from this software may not be called "ObjectStyle" or
 * "Cayenne", nor may "ObjectStyle" or "Cayenne" appear in their names without prior
 * written permission. THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE OBJECTSTYLE
 * GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ==================================================================== This software
 * consists of voluntary contributions made by many individuals and hosted on ObjectStyle
 * Group web site. For more information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 */
package org.objectstyle.cayenne;

import java.util.Collections;
import java.util.List;

import org.objectstyle.art.Artist;
import org.objectstyle.art.Painting;
import org.objectstyle.cayenne.access.DataContext;
import org.objectstyle.cayenne.exp.ExpressionFactory;
import org.objectstyle.cayenne.map.ObjEntity;
import org.objectstyle.cayenne.query.SelectQuery;
import org.objectstyle.cayenne.unit.CayenneTestCase;
import org.objectstyle.cayenne.unit.TestCaseDataFactory;

public class CayenneDataObjectInCtxtTst extends CayenneTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        deleteTestData();
    }

    public void testDoubleRegistration() {
        DataContext context = createDataContext();

        DataObject object = new Artist();
        assertNull(object.getObjectId());

        context.registerNewObject(object);
        ObjectId tempID = object.getObjectId();
        assertNotNull(tempID);
        assertTrue(tempID.isTemporary());
        assertSame(context, object.getDataContext());

        // double registration in the same context should be quietly ignored
        context.registerNewObject(object);
        assertSame(tempID, object.getObjectId());
        assertSame(object, context.getObjectStore().getObject(tempID));

        // registering in another context should throw an exception
        DataContext anotherContext = createDataContext();
        try {
            anotherContext.registerNewObject(object);
            fail("registerNewObject should've failed - object is already in another context");
        }
        catch (IllegalStateException e) {
            // expected
        }
    }

    public void testObjEntity() {
        DataContext context = createDataContext();

        Artist a = new Artist();
        assertNull(a.getObjEntity());

        context.registerNewObject(a);
        ObjEntity e = a.getObjEntity();
        assertNotNull(e);
        assertEquals("Artist", e.getName());

        Painting p = new Painting();
        assertNull(p.getObjEntity());

        context.registerNewObject(p);
        ObjEntity e1 = p.getObjEntity();
        assertNotNull(e1);
        assertEquals("Painting", e1.getName());
    }

    public void testResolveFault() {
        DataContext context = createDataContext();

        Artist o1 = newSavedArtist(context);
        context.invalidateObjects(Collections.singleton(o1));
        assertEquals(PersistenceState.HOLLOW, o1.getPersistenceState());
        assertNull(o1.readPropertyDirectly("artistName"));

        o1.resolveFault();
        assertEquals(PersistenceState.COMMITTED, o1.getPersistenceState());
        assertEquals("a", o1.readPropertyDirectly("artistName"));
    }

    public void testResolveFaultFailure() {
        DataContext context = createDataContext();

        DataObject o1 = context.registeredObject(new ObjectId(
                Artist.class,
                Artist.ARTIST_ID_PK_COLUMN,
                234));

        try {
            o1.resolveFault();
            fail("Must blow on non-existing fault.");
        }
        catch (CayenneRuntimeException ex) {

        }
    }

    public void testCommitChangesInBatch() {
        DataContext context = createDataContext();

        Artist a1 = (Artist) context.createAndRegisterNewObject("Artist");
        a1.setArtistName("abc1");

        Artist a2 = (Artist) context.createAndRegisterNewObject("Artist");
        a2.setArtistName("abc2");

        Artist a3 = (Artist) context.createAndRegisterNewObject("Artist");
        a3.setArtistName("abc3");

        context.commitChanges();

        List artists = context.performQuery(new SelectQuery(Artist.class));
        assertEquals(3, artists.size());
    }

    public void testSetObjectId() {
        DataContext context = createDataContext();

        Artist o1 = new Artist();
        assertNull(o1.getObjectId());

        context.registerNewObject(o1);
        assertNotNull(o1.getObjectId());
    }

    public void testStateTransToNew() {
        DataContext context = createDataContext();
        Artist o1 = new Artist();
        assertEquals(PersistenceState.TRANSIENT, o1.getPersistenceState());

        context.registerNewObject(o1);
        assertEquals(PersistenceState.NEW, o1.getPersistenceState());
    }

    public void testStateNewToCommitted() {
        DataContext context = createDataContext();

        Artist o1 = new Artist();
        o1.setArtistName("a");

        context.registerNewObject(o1);
        assertEquals(PersistenceState.NEW, o1.getPersistenceState());

        context.commitChanges();
        assertEquals(PersistenceState.COMMITTED, o1.getPersistenceState());
    }

    public void testStateCommittedToModified() {
        DataContext context = createDataContext();
        Artist o1 = new Artist();
        o1.setArtistName("a");
        context.registerNewObject(o1);
        context.commitChanges();
        assertEquals(PersistenceState.COMMITTED, o1.getPersistenceState());

        o1.setArtistName(o1.getArtistName() + "_1");
        assertEquals(PersistenceState.MODIFIED, o1.getPersistenceState());
    }

    public void testStateModifiedToCommitted() {
        DataContext context = createDataContext();

        Artist o1 = newSavedArtist(context);
        o1.setArtistName(o1.getArtistName() + "_1");
        assertEquals(PersistenceState.MODIFIED, o1.getPersistenceState());

        context.commitChanges();
        assertEquals(PersistenceState.COMMITTED, o1.getPersistenceState());
    }

    public void testStateCommittedToDeleted() {
        DataContext context = createDataContext();

        Artist o1 = new Artist();
        o1.setArtistName("a");
        context.registerNewObject(o1);
        context.commitChanges();
        assertEquals(PersistenceState.COMMITTED, o1.getPersistenceState());

        context.deleteObject(o1);
        assertEquals(PersistenceState.DELETED, o1.getPersistenceState());
    }

    public void testStateDeletedToTransient() {
        DataContext context = createDataContext();

        Artist o1 = newSavedArtist(context);
        context.deleteObject(o1);
        assertEquals(PersistenceState.DELETED, o1.getPersistenceState());

        context.commitChanges();
        assertEquals(PersistenceState.TRANSIENT, o1.getPersistenceState());
        assertFalse(context.getObjectStore().getObjects().contains(o1));
        assertNull(o1.getDataContext());
    }

    public void testSetDataContext() {
        DataContext context = createDataContext();

        Artist o1 = new Artist();
        assertNull(o1.getDataContext());

        context.registerNewObject(o1);
        assertSame(context, o1.getDataContext());
    }

    public void testFetchByAttr() throws Exception {
        DataContext context = createDataContext();

        String artistName = "artist with one painting";
        TestCaseDataFactory.createArtistWithPainting(artistName, new String[] {}, false);

        SelectQuery q = new SelectQuery("Artist", ExpressionFactory.matchExp(
                "artistName",
                artistName));

        List artists = context.performQuery(q);
        assertEquals(1, artists.size());
        Artist o1 = (Artist) artists.get(0);
        assertNotNull(o1);
        assertEquals(artistName, o1.getArtistName());
    }

    public void testUniquing() throws Exception {
        DataContext context = createDataContext();

        String artistName = "unique artist with no paintings";
        TestCaseDataFactory.createArtistWithPainting(artistName, new String[] {}, false);

        Artist a1 = fetchArtist(context, artistName);
        Artist a2 = fetchArtist(context, artistName);

        assertNotNull(a1);
        assertNotNull(a2);
        assertEquals(1, context.getObjectStore().getObjects().size());
        assertSame(a1, a2);
    }

    public void testSnapshotVersion1() {
        DataContext context = createDataContext();

        Artist artist = (Artist) context.createAndRegisterNewObject("Artist");
        assertEquals(DataObject.DEFAULT_VERSION, artist.getSnapshotVersion());

        // test versions set on commit

        artist.setArtistName("abc");
        context.commitChanges();

        assertEquals(context
                .getObjectStore()
                .getCachedSnapshot(artist.getObjectId())
                .getVersion(), artist.getSnapshotVersion());
    }

    public void testSnapshotVersion2() {
        DataContext context = createDataContext();

        newSavedArtist(context);

        // test versions assigned on fetch... clean up domain cache
        // before doing it
        getDomain().getEventManager().removeAllListeners(
                getDomain().getSharedSnapshotCache().getSnapshotEventSubject());
        getDomain().getSharedSnapshotCache().clear();
        context = getDomain().createDataContext();

        List artists = context.performQuery(new SelectQuery(Artist.class));
        Artist artist = (Artist) artists.get(0);

        assertFalse(DataObject.DEFAULT_VERSION == artist.getSnapshotVersion());
        assertEquals(context
                .getObjectStore()
                .getCachedSnapshot(artist.getObjectId())
                .getVersion(), artist.getSnapshotVersion());
    }

    public void testSnapshotVersion3() {
        DataContext context = createDataContext();

        Artist artist = newSavedArtist(context);

        // test versions assigned after update
        long oldVersion = artist.getSnapshotVersion();

        artist.setArtistName(artist.getArtistName() + "---");
        context.commitChanges();

        assertFalse(oldVersion == artist.getSnapshotVersion());
        assertEquals(context
                .getObjectStore()
                .getCachedSnapshot(artist.getObjectId())
                .getVersion(), artist.getSnapshotVersion());
    }

    /**
     * Tests a condition when user substitutes object id of a new object instead of
     * setting replacement. This is demonstrated here -
     * http://objectstyle.org/cayenne/lists/cayenne-user/2005/01/0210.html
     */
    public void testObjectsCommittedManualOID() {
        DataContext context = createDataContext();

        Artist object = (Artist) context.createAndRegisterNewObject(Artist.class);
        object.setArtistName("ABC1");
        assertEquals(PersistenceState.NEW, object.getPersistenceState());

        // do a manual id substitution
        object.setObjectId(new ObjectId(Artist.class, Artist.ARTIST_ID_PK_COLUMN, 3));

        context.commitChanges();
        assertEquals(PersistenceState.COMMITTED, object.getPersistenceState());

        // refetch
        context.invalidateObjects(Collections.singleton(object));

        Artist object2 = (Artist) DataObjectUtils.objectForPK(context, Artist.class, 3);
        assertNotNull(object2);
        assertEquals("ABC1", object2.getArtistName());
    }

    private Artist newSavedArtist(DataContext context) {
        Artist o1 = new Artist();
        o1.setArtistName("a");
        o1.setDateOfBirth(new java.sql.Date(System.currentTimeMillis()));
        context.registerNewObject(o1);
        context.commitChanges();
        return o1;
    }

    private Artist fetchArtist(DataContext context, String name) {
        SelectQuery q = new SelectQuery("Artist", ExpressionFactory.matchExp(
                "artistName",
                name));
        List ats = context.performQuery(q);
        return (ats.size() > 0) ? (Artist) ats.get(0) : null;
    }
}