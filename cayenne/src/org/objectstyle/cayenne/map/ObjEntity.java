package org.objectstyle.cayenne.map;
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


import java.util.*;
import org.objectstyle.cayenne.*;
import org.objectstyle.cayenne.exp.*;


/** Metadata for the data object class. Each data object class is tied
 *  to the relational table. The metadata for that relational table is
 *  in the corresponding "DbEntity". */
 public class ObjEntity extends Entity {
     private String className;
     private DbEntity dbEntity;
     private DataMap dataMap;

     public ObjEntity() {}

     public ObjEntity(String name) {
         setName(name);
     }


     /** Returns DataMap object that contains this ObjEntity. */
     public DataMap getDataMap() {
         return dataMap;
     }


     public void setDataMap(DataMap dataMap) {
         this.dataMap = dataMap;
     }


     /** Returns the name of the corresponding data object class */
     public String getClassName() {
         return className;
     }

     /** Sets the name of the data object class described by this obj entity*/
     public void setClassName(String className) {
         this.className = className;
     }


     /** Returns the corresponding db entity. */
     public DbEntity getDbEntity() {
         return dbEntity;
     }

     /** Set the db entity mapping. */
     public void setDbEntity(DbEntity dbEntity) {
         this.dbEntity = dbEntity;
     }


     /** Returns ObjAttribute of this entity that maps to <code>dbAttr</code>
       * parameter. Returns null if no such attribute is found. */
     public ObjAttribute getAttributeForDbAttribute(DbAttribute dbAttr) {
         Iterator it = getAttributeMap().values().iterator();
         while(it.hasNext()) {
             ObjAttribute objAttr = (ObjAttribute)it.next();
             if(objAttr.getDbAttribute() == dbAttr)
                 return objAttr;
         }
         return null;
    }

    /** Returns ObjRelationship of this entity that maps to <code>dbRel</code>
    * parameter. Returns null if no such relationship is found. */
    public ObjRelationship getRelationshipForDbRelationship(DbRelationship dbRel) {
        Iterator it = getRelationshipMap().values().iterator();
        while(it.hasNext()) {
            ObjRelationship objRel = (ObjRelationship)it.next();
            List relList = objRel.getDbRelationshipList();
            if(relList.size() != 1)
                continue;

            if(relList.get(0) == dbRel)
                return objRel;
        }
        return null;
    }


    /** Creates an object id from the values in object snapshot.
     * If needed attributes are missing in a snapshot or if it is null,
     * CayenneRuntimeException is thrown. */
    public ObjectId objectIdFromSnapshot(Map objectSnapshot) {
        HashMap idMap = new HashMap();
        Iterator it = getDbEntity().getPrimaryKey().iterator();
        while(it.hasNext()) {
            DbAttribute attr = (DbAttribute)it.next();
            Object val = objectSnapshot.get(attr.getName());
            if(val == null)
                throw new CayenneRuntimeException("Invalid snapshot value for '" + attr.getName() + "'. Must be present and not null.");

            idMap.put(attr.getName(), val);
        }

        return new ObjectId(this.getName(), idMap);
    }
    
     /** Clears all the mapping between this obj entity and its current db entity.
      *  Clears mapping between entities, attributes and relationships. */
     public void clearDbMapping() {
     	if (dbEntity == null)
     		return;

        Iterator it = getAttributeMap().values().iterator();
        while (it.hasNext()) {
             ObjAttribute objAttr = (ObjAttribute)it.next();
             DbAttribute dbAttr = objAttr.getDbAttribute();
             if (null != dbAttr) {
             	objAttr.setDbAttribute(null);
             }
        }// End while()
        
        Iterator rel_it = getRelationshipList().iterator();
        while(rel_it.hasNext()) {
        	ObjRelationship obj_rel = (ObjRelationship)rel_it.next();
        	obj_rel.removeAllDbRelationships();
        }
        dbEntity = null;
     }
    
}
