package com.mistraltech.smog.proxy.javassist.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Utility functions to simplify accessing Java reflection.
 */
public final class JavaReflectionUtils {

    private JavaReflectionUtils() {
    }

    /**
     * Create an instance of a class using the supplied constructor.
     *
     * @param constructor the constructor
     * @param <T> the type of the instance being constructed
     * @return the instance
     */
    public static <T> T createInstance(Constructor<T> constructor) {
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

    /**
     * Get the default constructor of the supplied class.
     *
     * @param clazz the class
     * @param <T> the class as a type parameter
     * @return the default constructor
     */
    public static <T> Constructor<T> getDefaultConstructor(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to get constructor method", e);
        }
    }

    /**
     * Create an instance of the supplied class using the default constructor.
     *
     * @param clazz class to instantiate
     * @param <T> the class to instantiate as a type parameter
     * @return an instance of the class
     */
    public static <T> T createInstance(Class<T> clazz) {
        final Constructor<T> constructor = JavaReflectionUtils.getDefaultConstructor(clazz);
        return JavaReflectionUtils.createInstance(constructor);
    }
}
