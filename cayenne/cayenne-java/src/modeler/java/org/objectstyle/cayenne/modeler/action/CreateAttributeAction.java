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
package org.objectstyle.cayenne.modeler.action;

import java.awt.event.ActionEvent;

import org.objectstyle.cayenne.map.DbAttribute;
import org.objectstyle.cayenne.map.DbEntity;
import org.objectstyle.cayenne.map.DerivedDbAttribute;
import org.objectstyle.cayenne.map.DerivedDbEntity;
import org.objectstyle.cayenne.map.Entity;
import org.objectstyle.cayenne.map.ObjAttribute;
import org.objectstyle.cayenne.map.ObjEntity;
import org.objectstyle.cayenne.map.event.AttributeEvent;
import org.objectstyle.cayenne.map.event.MapEvent;
import org.objectstyle.cayenne.modeler.Application;
import org.objectstyle.cayenne.modeler.ProjectController;
import org.objectstyle.cayenne.modeler.event.AttributeDisplayEvent;
import org.objectstyle.cayenne.modeler.util.CayenneAction;
import org.objectstyle.cayenne.project.NamedObjectFactory;
import org.objectstyle.cayenne.project.ProjectPath;

/**
 * @author Andrei Adamchik
 */
public class CreateAttributeAction extends CayenneAction {

    public static String getActionName() {
    	return "Create Attribute";
    }

    /**
     * Constructor for CreateAttributeAction.
     * @param name
     */
    public CreateAttributeAction(Application application) {
        super(getActionName(), application);
    }

    public String getIconName() {
        return "icon-attribute.gif";
    }

    /**
     * Creates ObjAttribute, DbAttribute depending on context.
     */
    public void performAction(ActionEvent e) {
        if (getProjectController().getCurrentObjEntity() != null) {
            createObjAttribute();
        }
        else if (getProjectController().getCurrentDbEntity() != null) {
            createDbAttribute();
        }
    }

    public void createObjAttribute() {
        ProjectController mediator = getProjectController();
        ObjEntity objEntity = mediator.getCurrentObjEntity();

        ObjAttribute attr = (ObjAttribute) NamedObjectFactory.createObject(
                ObjAttribute.class,
                objEntity);
        objEntity.addAttribute(attr);

        mediator.fireObjAttributeEvent(new AttributeEvent(
                this,
                attr,
                objEntity,
                MapEvent.ADD));

        AttributeDisplayEvent ade = new AttributeDisplayEvent(
                this,
                attr,
                objEntity,
                mediator.getCurrentDataMap(),
                mediator.getCurrentDataDomain());

        mediator.fireObjAttributeDisplayEvent(ade);
    }

    public void createDbAttribute() {
        Class attrClass = null;

        DbEntity dbEntity = getProjectController().getCurrentDbEntity();
        if (dbEntity instanceof DerivedDbEntity) {
            if (((DerivedDbEntity) dbEntity).getParentEntity() == null) {
                return;
            }
            attrClass = DerivedDbAttribute.class;
        }
        else {
            attrClass = DbAttribute.class;
        }

        DbAttribute attr =
            (DbAttribute) NamedObjectFactory.createObject(attrClass, dbEntity);
        dbEntity.addAttribute(attr);

        ProjectController mediator = getProjectController();
        mediator.fireDbAttributeEvent(
            new AttributeEvent(this, attr, dbEntity, MapEvent.ADD));

        AttributeDisplayEvent ade = new AttributeDisplayEvent(
                this,
                attr,
                dbEntity,
                mediator.getCurrentDataMap(),
                mediator.getCurrentDataDomain());

        mediator.fireDbAttributeDisplayEvent(ade);
    }

    /**
     * Returns <code>true</code> if path contains an Entity object.
     */
    public boolean enableForPath(ProjectPath path) {
        if (path == null) {
            return false;
        }

        return path.firstInstanceOf(Entity.class) != null;
    }
}
