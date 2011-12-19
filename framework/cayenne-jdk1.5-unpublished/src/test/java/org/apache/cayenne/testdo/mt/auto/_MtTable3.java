package org.apache.cayenne.testdo.mt.auto;

import java.util.List;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.testdo.mt.MtTable2;

/**
 * Class _MtTable3 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _MtTable3 extends CayenneDataObject {

    public static final String BINARY_COLUMN_PROPERTY = "binaryColumn";
    public static final String CHAR_COLUMN_PROPERTY = "charColumn";
    public static final String INT_COLUMN_PROPERTY = "intColumn";
    public static final String TABLE2ARRAY_PROPERTY = "table2Array";

    public static final String TABLE3_ID_PK_COLUMN = "TABLE3_ID";

    public void setBinaryColumn(byte[] binaryColumn) {
        writeProperty(BINARY_COLUMN_PROPERTY, binaryColumn);
    }
    public byte[] getBinaryColumn() {
        return (byte[])readProperty(BINARY_COLUMN_PROPERTY);
    }

    public void setCharColumn(String charColumn) {
        writeProperty(CHAR_COLUMN_PROPERTY, charColumn);
    }
    public String getCharColumn() {
        return (String)readProperty(CHAR_COLUMN_PROPERTY);
    }

    public void setIntColumn(Integer intColumn) {
        writeProperty(INT_COLUMN_PROPERTY, intColumn);
    }
    public Integer getIntColumn() {
        return (Integer)readProperty(INT_COLUMN_PROPERTY);
    }

    public void addToTable2Array(MtTable2 obj) {
        addToManyTarget(TABLE2ARRAY_PROPERTY, obj, true);
    }
    public void removeFromTable2Array(MtTable2 obj) {
        removeToManyTarget(TABLE2ARRAY_PROPERTY, obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<MtTable2> getTable2Array() {
        return (List<MtTable2>)readProperty(TABLE2ARRAY_PROPERTY);
    }


}
