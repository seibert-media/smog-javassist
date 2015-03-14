package com.mistraltech.smog.proxy.javassist;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

final class JavaClassUtils {
    public static <TM> TM createInstance(Constructor<TM> constructor) {
        try {
            return constructor.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to instantiate matcher class", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal access", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke constructor of generated matcher", e);
        }
    }

    public static <TM> Constructor<TM> getConstructor(Class<TM> matcherClass) {
        try {
            return matcherClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to get constructor method", e);
        }
    }
}
