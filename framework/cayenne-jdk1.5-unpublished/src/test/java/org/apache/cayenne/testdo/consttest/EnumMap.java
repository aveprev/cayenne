package org.apache.cayenne.testdo.consttest;

import org.apache.cayenne.testdo.consttest.auto._EnumMap;

public class EnumMap extends _EnumMap {

    private static EnumMap instance;

    private EnumMap() {}

    public static EnumMap getInstance() {
        if(instance == null) {
            instance = new EnumMap();
        }

        return instance;
    }
}
