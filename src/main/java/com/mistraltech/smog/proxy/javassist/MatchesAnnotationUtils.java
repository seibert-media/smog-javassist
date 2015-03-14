package com.mistraltech.smog.proxy.javassist;

import com.mistraltech.smog.core.annotation.Matches;
import javassist.CtClass;

final class MatchesAnnotationUtils {
    public static Matches getVerifiedMatchesAnnotation(CtClass matcherCtInterface) {
        final Matches matchesAnnotation = JavassistClassUtils.getAnnotation(matcherCtInterface, Matches.class);

        if (matchesAnnotation == null) {
            throw new IllegalArgumentException("Matcher interface is missing a @Matches annotation: " + matcherCtInterface.getName());
        }

        if (matchesAnnotation.value() == null) {
            throw new IllegalArgumentException("@Matches annotation on matcher interface has null value: " + matcherCtInterface.getName());
        }

        return matchesAnnotation;
    }

    public static String getMatchedClassDescription(Matches matchesAnnotation) {
        String description = matchesAnnotation.description();

        if (description == null) {
            description = "a " + matchesAnnotation.value().getSimpleName();
        }
        return description;
    }
}
