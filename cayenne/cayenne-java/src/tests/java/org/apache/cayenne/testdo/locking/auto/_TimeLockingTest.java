package org.apache.cayenne.testdo.locking.auto;

/** Class _TimeLockingTest was generated by Cayenne.
  * It is probably a good idea to avoid changing this class manually, 
  * since it may be overwritten next time code is regenerated. 
  * If you need to make any customizations, please use subclass. 
  */
public class _TimeLockingTest extends org.apache.cayenne.CayenneDataObject {

    public static final String NAME_PROPERTY = "name";
    public static final String TIME_LOCK_PROPERTY = "timeLock";

    public static final String TIME_LOCKING_TEST_ID_PK_COLUMN = "TIME_LOCKING_TEST_ID";

    public void setName(String name) {
        writeProperty("name", name);
    }
    public String getName() {
        return (String)readProperty("name");
    }
    
    
    public void setTimeLock(java.util.Date timeLock) {
        writeProperty("timeLock", timeLock);
    }
    public java.util.Date getTimeLock() {
        return (java.util.Date)readProperty("timeLock");
    }
    
    
}
