package com.mistraltech.smog.proxy.javassist;

import com.mistraltech.smog.core.CompositePropertyMatcher;
import com.mistraltech.smog.core.MatchAccumulator;
import com.mistraltech.smog.core.PropertyMatcher;
import com.mistraltech.smog.core.ReflectingPropertyMatcher;
import com.mistraltech.smog.core.annotation.Matches;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import org.hamcrest.Matcher;

import java.lang.reflect.Constructor;

import static com.mistraltech.smog.proxy.javassist.NameUtils.deCapitalise;
import static com.mistraltech.smog.proxy.javassist.NameUtils.removePrefix;

public class JavassistMatcherGeneratorImpl {

    public static <TM> TM matcherOf(Class<TM> matcherInterface) {
        Class<TM> matcherClass = getMatcherClass(matcherInterface);
        return createInstance(matcherClass);
    }

    @SuppressWarnings("unchecked")
    private static <TM> Class<TM> getMatcherClass(Class<TM> matcherInterface) {
        final String matcherClassName = matcherInterface.getName() + "SmogMatcher";

        try {
            return (Class<TM>) Class.forName(matcherClassName);
        } catch (ClassNotFoundException e) {
            return generateMatcherClass(matcherInterface, matcherClassName);
        }
    }

    private static <TM> TM createInstance(Class<TM> matcherClass) {
        final Constructor<TM> constructor = JavaClassUtils.getConstructor(matcherClass);
        return JavaClassUtils.createInstance(constructor);
    }

    private static <TM> Class<TM> generateMatcherClass(Class<TM> matcherInterface, String matcherClassName) {
        final CtClass matcherCtInterface = JavassistClassUtils.getCtClass(matcherInterface.getName());

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

        final Class<TM> matcherClass = JavassistClassUtils.getClassFrom(matcherCtClass);

        matcherCtClass.detach();

        return matcherClass;
    }

    private static void generateMatcherMethod(CtClass matcherCtClass, CtMethod ctMethod) {
        final String propertyName = toPropertyName(ctMethod.getName());
        final String propertyMatcherName = propertyName + "Matcher";
        final String propertyMatcherTypeName = PropertyMatcher.class.getTypeName();
        final String reflectingPropertyMatcherTypeName = ReflectingPropertyMatcher.class.getTypeName();
        final CtClass propertyMatcherCtClass = JavassistClassUtils.getCtClass(propertyMatcherTypeName);
        final String initializer = String.format("new %s(\"%s\", this)", reflectingPropertyMatcherTypeName, propertyName);

        if (!JavassistClassUtils.hasField(matcherCtClass, propertyMatcherName)) {
            JavassistClassUtils.addField(matcherCtClass, propertyMatcherCtClass, propertyMatcherName, initializer);
        }

        final CtClass[] parameterTypes = JavassistClassUtils.getParameterTypes(ctMethod);

        if (parameterTypes.length != 1) {
            throw new RuntimeException("Could not generate matcher method for " + ctMethod.getName() +
                    " - expected 1 parameter but got " + parameterTypes.length);
        }

        final CtClass hamcrestMatcherCtClass = JavassistClassUtils.getCtClass(Matcher.class.getTypeName());
        final boolean parameterIsMatcher = JavassistClassUtils.isSubTypeOf(parameterTypes[0], hamcrestMatcherCtClass);

        String body = generateMatcherMethodBody(propertyMatcherName, parameterIsMatcher);

        JavassistClassUtils.addMethod(matcherCtClass, Modifier.PUBLIC, ctMethod, body);
    }

    private static String generateMatcherMethodBody(String propertyMatcherName, boolean parameterIsMatcher) {
        if (parameterIsMatcher) {
            return String.format("{ this.%s.setMatcher($1); return this; }", propertyMatcherName);
        } else {
            return String.format("{ this.%s.setMatcher(org.hamcrest.CoreMatchers.equalTo(($w)$1)); return this; }", propertyMatcherName);
        }
    }

    private static String toPropertyName(String name) {
        if (!name.startsWith("has")) {
            throw new IllegalArgumentException("Expected method name to start with 'has'");
        }

        return deCapitalise(removePrefix(name, "has"));
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
