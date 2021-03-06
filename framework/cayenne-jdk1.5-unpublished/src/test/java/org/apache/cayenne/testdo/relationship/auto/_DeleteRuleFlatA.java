package org.apache.cayenne.testdo.relationship.auto;

import java.util.List;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.testdo.relationship.DeleteRuleFlatB;

/**
 * Class _DeleteRuleFlatA was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _DeleteRuleFlatA extends CayenneDataObject {

    public static final String FLAT_B_PROPERTY = "flatB";

    public static final String FLATA_ID_PK_COLUMN = "FLATA_ID";

    public void addToFlatB(DeleteRuleFlatB obj) {
        addToManyTarget(FLAT_B_PROPERTY, obj, true);
    }
    public void removeFromFlatB(DeleteRuleFlatB obj) {
        removeToManyTarget(FLAT_B_PROPERTY, obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<DeleteRuleFlatB> getFlatB() {
        return (List<DeleteRuleFlatB>)readProperty(FLAT_B_PROPERTY);
    }


}
