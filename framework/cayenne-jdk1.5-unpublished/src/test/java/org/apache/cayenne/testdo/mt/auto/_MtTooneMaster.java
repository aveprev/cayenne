package org.apache.cayenne.testdo.mt.auto;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.testdo.mt.MtTooneDep;

/**
 * Class _MtTooneMaster was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _MtTooneMaster extends CayenneDataObject {

    public static final String TO_DEPENDENT_PROPERTY = "toDependent";

    public static final String ID_PK_COLUMN = "ID";

    public void setToDependent(MtTooneDep toDependent) {
        setToOneTarget(TO_DEPENDENT_PROPERTY, toDependent, true);
    }

    public MtTooneDep getToDependent() {
        return (MtTooneDep)readProperty(TO_DEPENDENT_PROPERTY);
    }


}
