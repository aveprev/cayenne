package org.apache.cayenne.testdo.consttest.auto;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.testdo.consttest.UserType;

/**
 * Class _User was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _User extends CayenneDataObject {

    public static final String NAME_PROPERTY = "name";
    public static final String STATUS_PROPERTY = "status";
    public static final String TYPE_PROPERTY = "type";

    public static final String ID_PK_COLUMN = "ID";

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
    }

    public void setStatus(Integer status) {
        writeProperty("status", status);
    }
    public Integer getStatus() {
        return (Integer)readProperty("status");
    }

    public void setType(UserType type) {
        writeProperty("type", type);
    }
    public UserType getType() {
        return (UserType)readProperty("type");
    }

}