package com.mistraltech.smog.proxy.javassist;

import com.mistraltech.smog.core.CompositePropertyMatcher;
import com.mistraltech.smog.core.MatchAccumulator;
import com.mistraltech.smog.core.annotation.Matches;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;

import java.lang.reflect.Constructor;

public class JavassistMatcherGeneratorImpl {

    public static <TM> TM matcherOf(Class<TM> matcherInterface) {
        final String matcherInterfaceName = matcherInterface.getName();

        final String matcherClassName = matcherInterfaceName + "SmogMatcher";
        CtClass matcherCtClass = findCtClass(matcherClassName);

        if (matcherCtClass == null) {
            matcherCtClass = generateMatcherClass(matcherInterfaceName, matcherClassName);
        }

        return createInstance(matcherCtClass);
    }

    private static CtClass findCtClass(String matcherClassName) {
        return ClassPool.getDefault().getOrNull(matcherClassName);
    }

    private static <TM> TM createInstance(CtClass matcherCtClass) {
        final Class<TM> matcherClass = JavassistClassUtils.getClassFrom(matcherCtClass);
        final Constructor<TM> constructor = JavaClassUtils.getConstructor(matcherClass);
        return JavaClassUtils.createInstance(constructor);
    }

    private static CtClass generateMatcherClass(String matcherInterfaceName, String matcherClassName) {
        final CtClass matcherCtInterface = JavassistClassUtils.getCtClass(matcherInterfaceName);

        final Matches matchesAnnotation = MatchesAnnotationUtils.getVerifiedMatchesAnnotation(matcherCtInterface);

        final CtClass matchedCtClass = getMatchedClass(matchesAnnotation);
        final String matchedClassDescription = MatchesAnnotationUtils.getMatchedClassDescription(matchesAnnotation);
        final CtClass matcherCtSuperClass = JavassistClassUtils.getCtClass(CompositePropertyMatcher.class.getName());
        final CtClass matchAccumulatorCtClass = JavassistClassUtils.getCtClass(MatchAccumulator.class.getName());

        CtClass matcherCtClass = ClassPool.getDefault().makeClass(matcherClassName, matcherCtSuperClass);
        matcherCtClass.addInterface(matcherCtInterface);

        generateConstructor(matchedClassDescription, matcherCtClass);

        for (CtMethod ctMethod : matcherCtInterface.getDeclaredMethods()) {
            generateMatcherMethod(matcherCtClass, ctMethod);
        }

        generateMatchesSafelyMethod(matchedCtClass, matchAccumulatorCtClass, matcherCtClass);

        return matcherCtClass;
    }

    private static void generateMatcherMethod(CtClass matcherCtClass, CtMethod ctMethod) {
        String body = String.format("{ System.out.println(\"hello from %s\\n\"); return this; }", ctMethod.getName());
        JavassistClassUtils.addMethod(matcherCtClass, Modifier.PUBLIC, ctMethod, body);
    }

    private static void generateConstructor(String matchedClassDescription, CtClass matcherCtClass) {
        final String constructorBody = String.format("{ super(\"%s\"); }", matchedClassDescription);
        JavassistClassUtils.addConstructor(matcherCtClass, constructorBody);
    }

    private static void generateMatchesSafelyMethod(CtClass matchedCtClass, CtClass matchAccumulatorCtClass, CtClass matcherCtClass) {
        CtClass[] parameters = new CtClass[]{matchedCtClass, matchAccumulatorCtClass};
        JavassistClassUtils.addMethod(matcherCtClass, Modifier.PROTECTED, "matchesSafely", parameters, "{ super.matchesSafely($1, $2); }", CtClass.voidType);
    }

    private static CtClass getMatchedClass(Matches matchesAnnotation) {
        final Class<?> matchedClass = matchesAnnotation.value();
        return JavassistClassUtils.getCtClass(matchedClass.getName());
    }
}
