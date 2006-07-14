package org.objectstyle.cayenne.testdo.db2.auto;

import java.util.List;

/** Class _CrossdbM2E1 was generated by Cayenne.
  * It is probably a good idea to avoid changing this class manually, 
  * since it may be overwritten next time code is regenerated. 
  * If you need to make any customizations, please use subclass. 
  */
public class _CrossdbM2E1 extends org.objectstyle.cayenne.CayenneDataObject {

    public static final String NAME_PROPERTY = "name";
    public static final String LIST_OF_M2E2_PROPERTY = "listOfM2E2";

    public static final String ID_PK_COLUMN = "ID";

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
    }
    
    
    public void addToListOfM2E2(org.objectstyle.cayenne.testdo.db2.CrossdbM2E2 obj) {
        addToManyTarget("listOfM2E2", obj, true);
    }
    public void removeFromListOfM2E2(org.objectstyle.cayenne.testdo.db2.CrossdbM2E2 obj) {
        removeToManyTarget("listOfM2E2", obj, true);
    }
    public List getListOfM2E2() {
        return (List)readProperty("listOfM2E2");
    }
    
    
}
