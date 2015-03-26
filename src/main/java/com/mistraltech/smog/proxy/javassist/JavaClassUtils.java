package com.mistraltech.smog.proxy.javassist;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

final class JavaClassUtils {
    public static <TM> TM createInstance(Constructor<TM> constructor) {
        try {
            return constructor.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to instantiate class", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal access", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke constructor", e);
        }
    }

    public static <TM> Constructor<TM> getDefaultConstructor(Class<TM> clazz) {
        try {
            return clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to get constructor method", e);
        }
    }
}
