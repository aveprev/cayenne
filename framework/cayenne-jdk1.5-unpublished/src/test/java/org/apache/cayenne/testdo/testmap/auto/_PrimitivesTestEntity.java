package org.apache.cayenne.testdo.testmap.auto;

/** Class _PrimitivesTestEntity was generated by Cayenne.
  * It is probably a good idea to avoid changing this class manually, 
  * since it may be overwritten next time code is regenerated. 
  * If you need to make any customizations, please use subclass. 
  */
public abstract class _PrimitivesTestEntity extends org.apache.cayenne.CayenneDataObject {

    public static final String BOOLEAN_COLUMN_PROPERTY = "booleanColumn";
    public static final String INT_COLUMN_PROPERTY = "intColumn";

    public static final String ID_PK_COLUMN = "ID";

    public void setBooleanColumn(boolean booleanColumn) {
        writeProperty("booleanColumn", booleanColumn);
    }
	public boolean isBooleanColumn() {
        Boolean value = (Boolean)readProperty("booleanColumn");
        return (value != null) ? value.booleanValue() : false;
    }


    public void setIntColumn(int intColumn) {
        writeProperty("intColumn", intColumn);
    }
    public int getIntColumn() {
        Object value = readProperty("intColumn");
        return (value != null) ? (Integer) value : 0; 
    }


}
