package org.objectstyle.art.auto;

import org.objectstyle.art.GeneratedColumnCompMaster;
import org.objectstyle.cayenne.CayenneDataObject;

/** Class _GeneratedColumnCompKey was generated by Cayenne.
  * It is probably a good idea to avoid changing this class manually, 
  * since it may be overwritten next time code is regenerated. 
  * If you need to make any customizations, please use subclass. 
  */
public class _GeneratedColumnCompKey extends CayenneDataObject {

    public static final String NAME_PROPERTY = "name";
    public static final String TO_MASTER_PROPERTY = "toMaster";

    public static final String AUTO_PK_PK_COLUMN = "AUTO_PK";
    public static final String GENERATED_COLUMN_PK_COLUMN = "GENERATED_COLUMN";
    public static final String PROPAGATED_PK_PK_COLUMN = "PROPAGATED_PK";

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
    }
    
    
    public void setToMaster(GeneratedColumnCompMaster toMaster) {
        setToOneTarget("toMaster", toMaster, true);
    }

    public GeneratedColumnCompMaster getToMaster() {
        return (GeneratedColumnCompMaster)readProperty("toMaster");
    } 
    
    
}
