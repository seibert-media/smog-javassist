package com.mistraltech.smog.proxy.javassist;

import com.mistraltech.smog.core.annotation.Matches;
import javassist.CtClass;

class MatchesAnnotationUtils {
    static Matches getVerifiedMatchesAnnotation(CtClass matcherCtInterface) {
        final Matches matchesAnnotation = JavassistClassUtils.getMatchesAnnotation(matcherCtInterface, Matches.class);

        if (matchesAnnotation == null) {
            throw new IllegalArgumentException("Matcher interface is missing a @Matches annotation: " + matcherCtInterface.getName());
        }

        if (matchesAnnotation.value() == null) {
            throw new IllegalArgumentException("@Matches annotation on matcher interface has null value: " + matcherCtInterface.getName());
        }

        return matchesAnnotation;
    }

    static String getMatchedClassDescription(Matches matchesAnnotation) {
        String description = matchesAnnotation.description();

        if (description == null) {
            description = "a " + matchesAnnotation.value().getSimpleName();
        }
        return description;
    }
}
