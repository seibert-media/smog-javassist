package com.mistraltech.smog.proxy.javassist;

import com.mistraltech.smog.core.annotation.MatchesProperty;
import com.mistraltech.smog.proxy.javassist.util.JavassistClassUtils;
import javassist.CtClass;
import javassist.CtMethod;
import org.hamcrest.Matcher;

import static com.mistraltech.smog.proxy.javassist.util.NameUtils.deCapitalise;
import static com.mistraltech.smog.proxy.javassist.util.NameUtils.removePrefix;

public class MatcherMethodWrapper {
    private static final String MATCHER_METHOD_PREFIX = "has";

    private final CtMethod matcherMethod;

    public MatcherMethodWrapper(CtMethod matcherMethod) {
        this.matcherMethod = matcherMethod;
    }

    public String getMatchedPropertyName() {
        final MatchesProperty matchesPropertyAnnotation = JavassistClassUtils.getAnnotation(matcherMethod, MatchesProperty.class);

        if (matchesPropertyAnnotation != null) {
            return matchesPropertyAnnotation.value();
        }

        if (!hasMatcherMethodPropertyName(matcherMethod)) {
            throw new IllegalArgumentException(
                    String.format("Matcher method name '%s' was expected to start with prefix '%s'",
                            matcherMethod.getName(), MATCHER_METHOD_PREFIX));
        }

        return deCapitalise(removePrefix(matcherMethod.getName(), MATCHER_METHOD_PREFIX));
    }

    public static boolean hasMatcherMethodSignature(CtMethod ctMethod, CtClass matcherCtClass) {
        return hasMatcherMethodParameters(ctMethod)
                && hasMatcherMethodPropertyName(ctMethod)
                && hasMatcherMethodReturnType(ctMethod, matcherCtClass);
    }

    private static boolean hasMatcherMethodPropertyName(CtMethod ctMethod) {
        return ctMethod.hasAnnotation(MatchesProperty.class) || isConventionalMatcherMethodName(ctMethod.getName());
    }

    private static boolean isConventionalMatcherMethodName(String name) {
        return name.startsWith(MATCHER_METHOD_PREFIX);
    }

    private static boolean hasMatcherMethodParameters(CtMethod ctMethod) {
        final CtClass[] parameterTypes = JavassistClassUtils.getParameterTypes(ctMethod);
        return parameterTypes.length == 1;
    }

    private static boolean hasMatcherMethodReturnType(CtMethod ctMethod, CtClass matcherCtClass) {
        final CtClass returnType = JavassistClassUtils.getReturnType(ctMethod);
        final CtClass hamcrestMatcherCtClass = JavassistClassUtils.getCtClass(Matcher.class.getName());

        return JavassistClassUtils.isTypeInBounds(returnType, matcherCtClass, hamcrestMatcherCtClass);
    }

    public CtClass getParameterType() {
        return JavassistClassUtils.getSingleParameterType(matcherMethod);
    }

    public String getName() {
        return matcherMethod.getName();
    }

    public boolean takesHamcrestMatcher() {
        return isHamcrestMatcher(getParameterType());
    }

    private static boolean isHamcrestMatcher(CtClass parameterType) {
        final CtClass hamcrestMatcherCtClass = JavassistClassUtils.getCtClass(Matcher.class.getName());
        return JavassistClassUtils.isSubTypeOf(parameterType, hamcrestMatcherCtClass);
    }

    public CtMethod getCtMethod() {
        return matcherMethod;
    }
}
