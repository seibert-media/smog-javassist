package com.mistraltech.smog.proxy.javassist;

import com.mistraltech.smog.core.annotation.Matches;
import javassist.CtClass;

final class MatchesAnnotationUtils {
    public static Matches getVerifiedMatchesAnnotation(CtClass ctClass) {
        final Matches matchesAnnotation = JavassistClassUtils.getAnnotation(ctClass, Matches.class);

        if (matchesAnnotation == null) {
            throw new IllegalArgumentException("Missing @Matches annotation: " + ctClass.getName());
        }

        if (matchesAnnotation.value() == null) {
            throw new IllegalArgumentException("@Matches annotation has null value: " + ctClass.getName());
        }

        return matchesAnnotation;
    }

    public static String getMatchedClassDescription(Matches matchesAnnotation) {
        return matchesAnnotation.description() != null ?
                matchesAnnotation.description() : generateDescription(matchesAnnotation);
    }

    private static String generateDescription(Matches matchesAnnotation) {
        return "a " + matchesAnnotation.value().getSimpleName();
    }
}
