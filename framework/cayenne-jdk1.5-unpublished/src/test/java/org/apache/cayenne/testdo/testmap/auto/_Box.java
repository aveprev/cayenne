package org.apache.cayenne.testdo.testmap.auto;

import java.util.List;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.testdo.testmap.Bag;
import org.apache.cayenne.testdo.testmap.Ball;
import org.apache.cayenne.testdo.testmap.BoxInfo;
import org.apache.cayenne.testdo.testmap.Thing;

/**
 * Class _Box was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Box extends CayenneDataObject {

    public static final String BAG_PROPERTY = "bag";
    public static final String BALLS_PROPERTY = "balls";
    public static final String BOX_INFO_PROPERTY = "boxInfo";
    public static final String THINGS_PROPERTY = "things";

    public static final String ID_PK_COLUMN = "ID";

    public void setBag(Bag bag) {
        setToOneTarget("bag", bag, true);
    }

    public Bag getBag() {
        return (Bag)readProperty("bag");
    }


    public void addToBalls(Ball obj) {
        addToManyTarget("balls", obj, true);
    }
    public void removeFromBalls(Ball obj) {
        removeToManyTarget("balls", obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<Ball> getBalls() {
        return (List<Ball>)readProperty("balls");
    }


    public void setBoxInfo(BoxInfo boxInfo) {
        setToOneTarget("boxInfo", boxInfo, true);
    }

    public BoxInfo getBoxInfo() {
        return (BoxInfo)readProperty("boxInfo");
    }


    @SuppressWarnings("unchecked")
    public List<Thing> getThings() {
        return (List<Thing>)readProperty("things");
    }


}
