/* ====================================================================
 * 
 * The ObjectStyle Group Software License, version 1.1
 * ObjectStyle Group - http://objectstyle.org/
 * 
 * Copyright (c) 2002-2004, Andrei (Andrus) Adamchik and individual authors
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
package org.objectstyle.cayenne.modeler.datamap;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import org.objectstyle.cayenne.map.DataMap;
import org.objectstyle.cayenne.map.DbEntity;
import org.objectstyle.cayenne.map.DbRelationship;
import org.objectstyle.cayenne.map.event.DbEntityListener;
import org.objectstyle.cayenne.map.event.DbRelationshipListener;
import org.objectstyle.cayenne.map.event.EntityEvent;
import org.objectstyle.cayenne.map.event.RelationshipEvent;
import org.objectstyle.cayenne.modeler.PanelFactory;
import org.objectstyle.cayenne.modeler.control.EventController;
import org.objectstyle.cayenne.modeler.event.DbEntityDisplayListener;
import org.objectstyle.cayenne.modeler.event.EntityDisplayEvent;
import org.objectstyle.cayenne.modeler.event.RelationshipDisplayEvent;
import org.objectstyle.cayenne.modeler.util.CayenneTable;
import org.objectstyle.cayenne.modeler.util.CayenneWidgetFactory;

/** 
 * Displays DbRelationships for the current DbEntity. 
 * 
 * @author Michael Misha Shengaout
 * @author Andrei Adamchik
 */
public class DbRelationshipPane
    extends JPanel
    implements
        ActionListener,
        DbEntityDisplayListener,
        DbEntityListener,
        DbRelationshipListener,
        ExistingSelectionProcessor,
        ListSelectionListener,
        TableModelListener {

    protected EventController mediator;
    protected CayenneTable table;
    protected JButton resolve;

    public DbRelationshipPane(EventController mediator) {
        super();

        this.mediator = mediator;
        this.mediator.addDbEntityDisplayListener(this);
        this.mediator.addDbEntityListener(this);
        this.mediator.addDbRelationshipListener(this);

        init();
        resolve.addActionListener(this);
    }

    protected void init() {
        setLayout(new BorderLayout());
        table = new CayenneTable();
        resolve = new JButton("Database Mapping");
        JPanel panel = PanelFactory.createTablePanel(table, new JButton[] { resolve });
        add(panel, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == resolve) {
            resolveRelationship();
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        processExistingSelection();
    }

    public void tableChanged(TableModelEvent e) {
        DbRelationship rel = null;
        if (table.getSelectedRow() >= 0) {
            DbRelationshipTableModel model = (DbRelationshipTableModel) table.getModel();
            rel = model.getRelationship(table.getSelectedRow());
            if (rel.getTargetEntity() != null)
                resolve.setEnabled(true);
            else
                resolve.setEnabled(false);
        }
    }

    /**
     * Selects a specified relationship in the relationships table.
     */
    public void selectRelationship(DbRelationship rel) {
        if (rel == null) {
            return;
        }

        DbRelationshipTableModel model = (DbRelationshipTableModel) table.getModel();
        java.util.List rels = model.getObjectList();
        int relPos = rels.indexOf(rel);
        if (relPos >= 0) {
            table.select(relPos);
        }
    }

    public void processExistingSelection() {
        DbRelationship rel = null;
        if (table.getSelectedRow() >= 0) {
            DbRelationshipTableModel model;
            model = (DbRelationshipTableModel) table.getModel();
            rel = model.getRelationship(table.getSelectedRow());
            if (rel.getTargetEntity() != null)
                resolve.setEnabled(true);
            else
                resolve.setEnabled(false);

            // scroll table
            table.scroll(table.getSelectedRow(), 0);
        }
        else
            resolve.setEnabled(false);

        RelationshipDisplayEvent ev =
            new RelationshipDisplayEvent(
                this,
                rel,
                mediator.getCurrentDbEntity(),
                mediator.getCurrentDataMap(),
                mediator.getCurrentDataDomain());

        mediator.fireDbRelationshipDisplayEvent(ev);
    }

    private void resolveRelationship() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }

        // Get DbRelationship
        DbRelationshipTableModel model = (DbRelationshipTableModel) table.getModel();
        DbRelationship rel = model.getRelationship(row);
        DbEntity start = (DbEntity) rel.getSourceEntity();
        DbEntity end = (DbEntity) rel.getTargetEntity();

        java.util.List dbRelList = new ArrayList();
        dbRelList.add(rel);

        ResolveDbRelationshipDialog dialog =
            new ResolveDbRelationshipDialog(dbRelList, start, end, rel.isToMany());
        dialog.setVisible(true);
        dialog.dispose();
    }

    /** Loads obj relationships into table. */
    public void currentDbEntityChanged(EntityDisplayEvent e) {
        DbEntity entity = (DbEntity) e.getEntity();
        if (entity != null && e.isEntityChanged()) {
            rebuildTable(entity);
        }

        // if an entity was selected on a tree, 
        // unselect currently selected row
        if (e.isUnselectAttributes()) {
            table.clearSelection();
        }
    }

    protected void rebuildTable(DbEntity dbEnt) {
        DbRelationshipTableModel model =
            new DbRelationshipTableModel(dbEnt, mediator, this);
        model.addTableModelListener(this);
        table.setModel(model);
        table.setRowHeight(25);
        table.setRowMargin(3);
        TableColumn col = table.getColumnModel().getColumn(DbRelationshipTableModel.NAME);
        col.setMinWidth(150);
        col = table.getColumnModel().getColumn(DbRelationshipTableModel.TARGET);
        col.setMinWidth(150);
        JComboBox combo = CayenneWidgetFactory.createComboBox(createComboModel(), false);
        combo.setEditable(false);
        col.setCellEditor(new DefaultCellEditor(combo));
        table.getSelectionModel().addListSelectionListener(this);
    }

    /** 
     * Creates a list of DbEntity names.
     */
    private Object[] createComboModel() {
        DataMap map = mediator.getCurrentDataMap();
        Collection dbEntities = map.getDbEntities(true);
        int len = dbEntities.size();
        Object[] names = dbEntities.toArray();

        for (int i = 0; i < len; i++) {
            // substitute DbEntities with their names
            names[i] = ((DbEntity) names[i]).getName();
        }

        Arrays.sort(names);

        return names;
    }

    public void dbEntityChanged(EntityEvent e) {
    }

    public void dbEntityAdded(EntityEvent e) {
        reloadEntityList(e);
    }
    public void dbEntityRemoved(EntityEvent e) {
        reloadEntityList(e);
    }

    public void dbRelationshipChanged(RelationshipEvent e) {
        if (e.getSource() != this) {
            if (!(table.getModel() instanceof DbRelationshipTableModel)) {
                rebuildTable((DbEntity) e.getEntity());
            }

            table.select(e.getRelationship());
            DbRelationshipTableModel model = (DbRelationshipTableModel) table.getModel();
            model.fireTableDataChanged();
        }
    }

    public void dbRelationshipAdded(RelationshipEvent e) {
        rebuildTable((DbEntity) e.getEntity());
        table.select(e.getRelationship());
    }

    public void dbRelationshipRemoved(RelationshipEvent e) {
        DbRelationshipTableModel model = (DbRelationshipTableModel) table.getModel();
        int ind = model.getObjectList().indexOf(e.getRelationship());
        model.removeRelationship(e.getRelationship());
        table.select(ind);
    }

    /** Refresh the list of db entities (targets). 
      * Also refresh the table in case some db relationships were deleted.*/
    private void reloadEntityList(EntityEvent e) {
        if (e.getSource() == this)
            return;
        // If current model added/removed, do nothing.
        if (mediator.getCurrentDbEntity() == e.getEntity())
            return;
        // If this is just loading new currentDbEntity, do nothing
        if (mediator.getCurrentDbEntity() == null)
            return;
        TableColumn col =
            table.getColumnModel().getColumn(DbRelationshipTableModel.TARGET);
        DefaultCellEditor editor = (DefaultCellEditor) col.getCellEditor();
        JComboBox combo = (JComboBox) editor.getComponent();
        combo.setModel(new DefaultComboBoxModel(createComboModel()));

        DbRelationshipTableModel model = (DbRelationshipTableModel) table.getModel();
        model.fireTableDataChanged();
        table.getSelectionModel().addListSelectionListener(this);
    }
}