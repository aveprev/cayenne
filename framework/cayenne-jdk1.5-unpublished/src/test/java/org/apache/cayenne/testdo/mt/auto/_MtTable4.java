package org.apache.cayenne.testdo.mt.auto;

import java.util.List;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.testdo.mt.MtTable5;

/**
 * Class _MtTable4 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _MtTable4 extends CayenneDataObject {

    public static final String TABLE5S_PROPERTY = "table5s";

    public static final String ID_PK_COLUMN = "ID";

    public void addToTable5s(MtTable5 obj) {
        addToManyTarget(TABLE5S_PROPERTY, obj, true);
    }
    public void removeFromTable5s(MtTable5 obj) {
        removeToManyTarget(TABLE5S_PROPERTY, obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<MtTable5> getTable5s() {
        return (List<MtTable5>)readProperty(TABLE5S_PROPERTY);
    }


}
