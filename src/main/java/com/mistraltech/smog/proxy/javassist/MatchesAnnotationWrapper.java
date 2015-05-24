package com.mistraltech.smog.proxy.javassist;

import com.mistraltech.smog.core.annotation.Matches;

/**
 * Wrapper for accessing properties of the SMOG @Matches annotation.
 */
class MatchesAnnotationWrapper {

    private final Matches annotation;

    /**
     * Constructor.
     *
     * @param owningClass the class having the @Matches annotation
     */
    public MatchesAnnotationWrapper(Class<?> owningClass) {
        this.annotation = getVerifiedMatchesAnnotation(owningClass);
    }

    /**
     * Get the {@link Matches} annotation on the supplied class. Performs basic validation checks, ensuring that
     * the annotation is present and has a non-null value.
     *
     * @param clazz the class
     * @return the annotation
     */
    private Matches getVerifiedMatchesAnnotation(Class<?> clazz) {
        final Matches matchesAnnotation = clazz.getAnnotation(Matches.class);

        if (matchesAnnotation == null) {
            throw new IllegalArgumentException("Missing @Matches annotation: " + clazz.getName());
        }

        if (matchesAnnotation.value() == null) {
            throw new IllegalArgumentException("@Matches annotation has null value: " + clazz.getName());
        }

        return matchesAnnotation;
    }

    /**
     * Gets the matched class.
     *
     * @return the class that the annotation declares is being matched
     */
    public Class<?> getMatchedClass() {
        return annotation.value();
    }

    /**
     * Gets the description of the matched class.
     *
     * @return matched class description, e.g. "a Widget"
     */
    public String getMatchedClassDescription() {
        return annotation.description() != null ? annotation.description() : generateDescription();
    }

    private String generateDescription() {
        return "a " + annotation.value().getSimpleName();
    }
}
