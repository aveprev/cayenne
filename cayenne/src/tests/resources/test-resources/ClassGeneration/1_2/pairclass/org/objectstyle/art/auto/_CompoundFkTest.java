package org.objectstyle.art.auto;

import org.objectstyle.art.CompoundPkTest;
import org.objectstyle.cayenne.CayenneDataObject;

/** Class _CompoundFkTest was generated by Cayenne.
  * It is probably a good idea to avoid changing this class manually, 
  * since it may be overwritten next time code is regenerated. 
  * If you need to make any customizations, please use subclass. 
  */
public class _CompoundFkTest extends CayenneDataObject {

    public static final String NAME_PROPERTY = "name";
    public static final String TO_COMPOUND_PK_PROPERTY = "toCompoundPk";

    public static final String PKEY_PK_COLUMN = "PKEY";

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
    }
    
    
    public void setToCompoundPk(CompoundPkTest toCompoundPk) {
        setToOneTarget("toCompoundPk", toCompoundPk, true);
    }

    public CompoundPkTest getToCompoundPk() {
        return (CompoundPkTest)readProperty("toCompoundPk");
    } 
    
    
}
