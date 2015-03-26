package com.mistraltech.smog.proxy.javassist;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

final class JavassistClassUtils {
    public static void addConstructor(CtClass ctClass, String constructorBody) {
        try {
            final CtConstructor ctConstructor = CtNewConstructor.make(null, null, constructorBody, ctClass);
            ctClass.addConstructor(ctConstructor);
        } catch (CannotCompileException e) {
            throw new RuntimeException("Failed to compile class constructor", e);
        }
    }

    public static void addMethod(CtClass ctClass, int modifiers, String methodName, CtClass[] parameters, String body, CtClass returnType) {
        try {
            final CtMethod method = CtNewMethod.make(modifiers, returnType, methodName, parameters, null, body, ctClass);
            ctClass.addMethod(method);
        } catch (CannotCompileException e) {
            throw new RuntimeException("Failed to compile class", e);
        }
    }

    public static CtClass getCtClass(String className) {
        try {
            return ClassPool.getDefault().get(className);
        } catch (NotFoundException e) {
            throw new RuntimeException("Failed to find class", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <TM> Class<TM> getClassFrom(CtClass matcherCtClass) {
        try {
            return matcherCtClass.toClass();
        } catch (CannotCompileException e) {
            throw new RuntimeException("Failed to compile class", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAnnotation(CtClass ctClass, Class<T> annotationClass) {
        try {
            return (T) ctClass.getAnnotation(annotationClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find annotation class", e);
        }
    }

    public static boolean hasField(CtClass ctClass, String fieldName) {
        try {
            ctClass.getField(fieldName);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    public static void addField(CtClass ctClass, CtClass type, String fieldName, String initializer) {
        try {
            final CtField field = new CtField(type, fieldName, ctClass);
            field.setModifiers(Modifier.FINAL & Modifier.PRIVATE);
            ctClass.addField(field, initializer);
        } catch (CannotCompileException e) {
            throw new RuntimeException("Failed to compile field", e);
        }
    }

    public static void addMethod(CtClass ctClass, int modifiers, CtMethod ctMethod, String body) {
        try {
            addMethod(ctClass, modifiers, ctMethod.getName(), ctMethod.getParameterTypes(), body, ctMethod.getReturnType());
        } catch (NotFoundException e) {
            throw new RuntimeException("Failed to find class", e);
        }
    }

    public static CtClass[] getParameterTypes(CtMethod ctMethod) {
        try {
            return ctMethod.getParameterTypes();
        } catch (NotFoundException e) {
            throw new RuntimeException("Could not get parameter types for method " + ctMethod.getName(), e);
        }
    }

    public static boolean isSubTypeOf(CtClass ctClass, CtClass superTypeCtClass) {
        try {
            return ctClass.subtypeOf(superTypeCtClass);
        } catch (NotFoundException e) {
            throw new RuntimeException("Failed to find class", e);
        }
    }

    public static CtClass getReturnType(CtMethod ctMethod) {
        try {
            return ctMethod.getReturnType();
        } catch (NotFoundException e) {
            throw new RuntimeException("Failed to find class", e);
        }
    }

    public static boolean isTypeInBounds(CtClass boundedType, CtClass lowerBound, CtClass upperBound) {
        try {
            return lowerBound.subtypeOf(boundedType) && boundedType.subtypeOf(upperBound);
        } catch (NotFoundException e) {
            throw new RuntimeException("Failed to find class", e);
        }
    }

    public static CtClass[] getInterfaces(CtClass ctClass) {
        try {
            return ctClass.getInterfaces();
        } catch (NotFoundException e) {
            throw new RuntimeException("Failed to find class", e);
        }
    }

    public static CtClass getSuperclass(CtClass ctClass) {
        try {
            return ctClass.getSuperclass();
        } catch (NotFoundException e) {
            throw new RuntimeException("Failed to find class", e);
        }
    }
}
