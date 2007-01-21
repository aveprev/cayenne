package org.apache.cayenne.testdo.relationship.auto;

import java.util.List;

/** Class _FlattenedTest2 was generated by Cayenne.
  * It is probably a good idea to avoid changing this class manually, 
  * since it may be overwritten next time code is regenerated. 
  * If you need to make any customizations, please use subclass. 
  */
public class _FlattenedTest2 extends org.apache.cayenne.CayenneDataObject {

    public static final String NAME_PROPERTY = "name";
    public static final String FT3ARRAY_PROPERTY = "ft3Array";
    public static final String TO_FT1_PROPERTY = "toFT1";

    public static final String FT2_ID_PK_COLUMN = "FT2_ID";

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
    }
    
    
    public void addToFt3Array(org.apache.cayenne.testdo.relationship.FlattenedTest3 obj) {
        addToManyTarget("ft3Array", obj, true);
    }
    public void removeFromFt3Array(org.apache.cayenne.testdo.relationship.FlattenedTest3 obj) {
        removeToManyTarget("ft3Array", obj, true);
    }
    public List getFt3Array() {
        return (List)readProperty("ft3Array");
    }
    
    
    public void setToFT1(org.apache.cayenne.testdo.relationship.FlattenedTest1 toFT1) {
        setToOneTarget("toFT1", toFT1, true);
    }

    public org.apache.cayenne.testdo.relationship.FlattenedTest1 getToFT1() {
        return (org.apache.cayenne.testdo.relationship.FlattenedTest1)readProperty("toFT1");
    } 
    
    
}
