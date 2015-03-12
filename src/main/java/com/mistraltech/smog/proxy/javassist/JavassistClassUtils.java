package com.mistraltech.smog.proxy.javassist;

import javassist.*;

class JavassistClassUtils {
    static void addConstructor(CtClass matcherCtClass, String constructorBody) {
        try {
            final CtConstructor ctConstructor = CtNewConstructor.make(null, null, constructorBody, matcherCtClass);
            matcherCtClass.addConstructor(ctConstructor);
        } catch (CannotCompileException e) {
            throw new RuntimeException("Failed to compile matcher class", e);
        }
    }

    static void addMethod(CtClass matcherCtClass, int modifiers, String methodName, CtClass[] parameters, String body, CtClass returnType) {
        try {
            final CtMethod method = CtNewMethod.make(modifiers, returnType, methodName, parameters, null, body, matcherCtClass);
            matcherCtClass.addMethod(method);
        } catch (CannotCompileException e) {
            throw new RuntimeException("Failed to compile matcher class", e);
        }
    }

    static CtClass getCtClass(String matcherInterfaceName) {
        try {
            return ClassPool.getDefault().get(matcherInterfaceName);
        } catch (NotFoundException e) {
            throw new RuntimeException("Failed to find class", e);
        }
    }

    @SuppressWarnings("unchecked")
    static <TM> Class<TM> getClassFrom(CtClass matcherCtClass) {
        try {
            return matcherCtClass.toClass();
        } catch (CannotCompileException e) {
            throw new RuntimeException("Failed to compile matcher class", e);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T getMatchesAnnotation(CtClass matcherCtInterface, Class<T> annotationClass) {
        try {
            return (T) matcherCtInterface.getAnnotation(annotationClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find annotation class", e);
        }
    }

    public static void addMethod(CtClass matcherCtClass, int modifiers, CtMethod ctMethod, String body) {
        try {
            addMethod(matcherCtClass, modifiers, ctMethod.getName(), ctMethod.getParameterTypes(), body, ctMethod.getReturnType());
        } catch (NotFoundException e) {
            throw new RuntimeException("Failed to find class", e);
        }
    }
}
