package com.github.rudder.shared;


import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Method;

public class Util {

    public static Method findMethod(final Class cls,
                                    final String methodName,
                                    final Class[] parameterTypes) {

        final Method matchingMethod = MethodUtils.getMatchingMethod(cls, methodName, parameterTypes);
        matchingMethod.setAccessible(true);
        return matchingMethod;
    }

    public static boolean isEmpty(final String string) {
        return string == null || string.isEmpty();
    }

}
