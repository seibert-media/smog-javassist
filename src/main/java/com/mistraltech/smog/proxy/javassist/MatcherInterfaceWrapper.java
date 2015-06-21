package com.mistraltech.smog.proxy.javassist;

import com.mistraltech.smog.proxy.javassist.util.JavassistClassUtils;
import javassist.CtClass;
import javassist.CtMethod;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps and augments the matcher interface.
 */
public class MatcherInterfaceWrapper<TM> {
    private final MatchesAnnotationWrapper matchesAnnotation;
    private final CtClass matcherCtInterface;

    public MatcherInterfaceWrapper(Class<TM> matcherInterface) {
        this.matchesAnnotation = new MatchesAnnotationWrapper(matcherInterface);
        this.matcherCtInterface = JavassistClassUtils.getCtClass(matcherInterface.getName());
    }

    public CtClass getCtInterface() {
        return matcherCtInterface;
    }

    public String getMatchedClassDescription() {
        return matchesAnnotation.getMatchedClassDescription();
    }

    public List<MatcherMethodWrapper> getMatcherMethods() {
        List<MatcherMethodWrapper> methods = new ArrayList<MatcherMethodWrapper>();

        for (CtMethod method : JavassistClassUtils.getMethods(matcherCtInterface)) {
            if (MatcherMethodWrapper.hasMatcherMethodSignature(method, matcherCtInterface)) {
                methods.add(new MatcherMethodWrapper(method));
            }
        }

        return methods;
    }

    public CtClass getMatchedClass() {
        final Class<?> matchedClass = matchesAnnotation.getMatchedClass();
        return JavassistClassUtils.getCtClass(matchedClass.getName());
    }

    public List<CtMethod> getLikeMethods() {
        List<CtMethod> methods = new ArrayList<CtMethod>();

        CtClass matchedCtClass = getMatchedClass();

        for (CtMethod method : JavassistClassUtils.getMethods(matcherCtInterface)) {
            if (hasLikeMethodSignature(method, matchedCtClass, matcherCtInterface)) {
                methods.add(method);
            }
        }

        return methods;
    }

    private static boolean hasLikeMethodSignature(CtMethod ctMethod, CtClass matchedCtClass, CtClass matcherCtClass) {
        final CtClass returnType = JavassistClassUtils.getReturnType(ctMethod);
        final CtClass[] parameterTypes = JavassistClassUtils.getParameterTypes(ctMethod);

        return parameterTypes.length == 1 &&
                ctMethod.getName().equals("like") &&
                JavassistClassUtils.isTypeInBounds(returnType, matcherCtClass, SmogTypes.getHamcrestMatcherCtClass()) &&
                JavassistClassUtils.isSubTypeOf(matchedCtClass, parameterTypes[0]);
    }
}
