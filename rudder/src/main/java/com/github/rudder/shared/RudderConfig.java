package com.github.rudder.shared;

import java.util.ArrayList;
import java.util.List;

public class RudderConfig {

    /**
     * All primitive object classes
     */
    private static List<Class<?>> PRIMITIVES = new ArrayList<>();

    static {
        PRIMITIVES.add(CharSequence.class);
        PRIMITIVES.add(Number.class);
        PRIMITIVES.add(Boolean.class);
    }

    /**
     * Should object be considered as a primitive and passed as JSON between client and server
     *
     * @param object some object
     * @return is it a primitive
     */
    public static boolean isPrimitive(final Object object) {
        for (Class<?> primitiveClass : PRIMITIVES) {
            if (primitiveClass.isInstance(object)) {
                return true;
            }
        }
        return false;
    }

}
