package org.apache.cayenne.testdo.enumtest;

import org.apache.cayenne.testdo.enumtest.auto._EnumMap;

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
