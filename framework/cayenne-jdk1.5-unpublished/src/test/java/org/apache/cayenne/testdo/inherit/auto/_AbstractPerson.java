package org.apache.cayenne.testdo.inherit.auto;

import java.util.List;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.testdo.inherit.PersonNotes;

/**
 * Class _AbstractPerson was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _AbstractPerson extends CayenneDataObject {

    public static final String NAME_PROPERTY = "name";
    public static final String PERSON_TYPE_PROPERTY = "personType";
    public static final String NOTES_PROPERTY = "notes";

    public static final String PERSON_ID_PK_COLUMN = "PERSON_ID";

    public void setName(String name) {
        writeProperty(NAME_PROPERTY, name);
    }
    public String getName() {
        return (String)readProperty(NAME_PROPERTY);
    }

    public void setPersonType(String personType) {
        writeProperty(PERSON_TYPE_PROPERTY, personType);
    }
    public String getPersonType() {
        return (String)readProperty(PERSON_TYPE_PROPERTY);
    }

    public void addToNotes(PersonNotes obj) {
        addToManyTarget(NOTES_PROPERTY, obj, true);
    }
    public void removeFromNotes(PersonNotes obj) {
        removeToManyTarget(NOTES_PROPERTY, obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<PersonNotes> getNotes() {
        return (List<PersonNotes>)readProperty(NOTES_PROPERTY);
    }


}
