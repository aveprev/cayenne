/* ====================================================================
 *
 * The ObjectStyle Group Software License, Version 1.0
 *
 * Copyright (c) 2002 The ObjectStyle Group
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

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;

import org.apache.log4j.Level;
import org.objectstyle.art.Artist;
import org.objectstyle.art.Painting;
import org.objectstyle.cayenne.map.Procedure;
import org.objectstyle.cayenne.map.ProcedureParam;
import org.objectstyle.cayenne.query.ProcedureQuery;
import org.objectstyle.cayenne.query.ProcedureSelectQuery;
import org.objectstyle.cayenne.query.SelectQuery;
import org.objectstyle.cayenne.unittest.CayenneTestCase;

/**
 * @author Andrei Adamchik
 */
public class DataContextStoredProcTst extends CayenneTestCase {
    public static final String UPDATE_STORED_PROCEDURE = "cayenne_tst_upd_proc";
    public static final String SELECT_STORED_PROCEDURE =
        "cayenne_tst_select_proc";

    protected DataContext ctxt;

    /**
     * Constructor for DataContextStoredProcTst.
     * @param name
     */
    public DataContextStoredProcTst(String name) {
        super(name);
    }

    public void testUpdate() throws Exception {
        // Don't run this on MySQL
        if (!getDatabaseSetupDelegate().supportsStoredProcedures()) {
            return;
        }

        // create an artist with painting in the database  
        createArtist(1000.0);

        // create and run stored procedure
        Procedure proc = new Procedure(UPDATE_STORED_PROCEDURE);
        ProcedureParam param = new ProcedureParam("paintingPrice", Types.INTEGER, ProcedureParam.IN_PARAM);
        proc.addCallParam(param);

        ProcedureQuery q = new ProcedureQuery(Artist.class, proc);
        q.addParam("paintingPrice", new Integer(3000));
        DefaultOperationObserver observer = new DefaultOperationObserver();
        observer.setLoggingLevel(Level.WARN);
        ctxt.performQuery(q, observer);

        // check that price have doubled
        SelectQuery select = new SelectQuery(Artist.class);
        select.addPrefetch("paintingArray");
        select.setLoggingLevel(Level.WARN);
        List artists = ctxt.performQuery(select);
        assertEquals(1, artists.size());

        Artist a = (Artist) artists.get(0);
        Painting p = (Painting) a.getPaintingArray().get(0);
        assertEquals(2000, p.getEstimatedPrice().intValue());
    }

    public void testSelect() throws Exception {
        // Don't run this on MySQL
        if (!getDatabaseSetupDelegate().supportsStoredProcedures()) {
            return;
        }

        // create an artist with painting in the database
        createArtist(1000.0);

        // create and run stored procedure
        Procedure proc = new Procedure(SELECT_STORED_PROCEDURE);
        ProcedureParam param1 = new ProcedureParam("aName", Types.VARCHAR, ProcedureParam.IN_PARAM);
        ProcedureParam param2 = new ProcedureParam("paintingPrice", Types.INTEGER, ProcedureParam.IN_PARAM);
        proc.addCallParam(param1);
        proc.addCallParam(param2);

        ProcedureSelectQuery q = new ProcedureSelectQuery(Artist.class, proc);
        q.addParam("aName", "An Artist");
        q.addParam("paintingPrice", new Integer(3000));
        q.setLoggingLevel(Level.WARN);
         // List artists = ctxt.performQuery(q);

        // check the results
        /*assertNotNull("Null result from StoredProcedure.", artists);
        assertEquals(1, artists.size());
        Artist a = (Artist) artists.get(0);
        Painting p = (Painting) a.getPaintingArray().get(0);
        assertEquals(2000, p.getEstimatedPrice().intValue());*/
    }

    protected void createArtist(double paintingPrice) {
        Artist a = (Artist) ctxt.createAndRegisterNewObject("Artist");
        a.setArtistName("An Artist");

        Painting p = (Painting) ctxt.createAndRegisterNewObject("Painting");
        p.setPaintingTitle("A Painting");
        p.setEstimatedPrice(new BigDecimal(paintingPrice));
        a.addToPaintingArray(p);

        ctxt.commitChanges();
    }

    protected void setUp() throws Exception {
        // Don't run this on MySQL
        if (!getDatabaseSetupDelegate().supportsStoredProcedures()) {
            return;
        }

        getDatabaseSetup().cleanTableData();
        ctxt = createDataContext();
    }

}
