package com.mistraltech.smog.examples.generics.matcher;

import com.mistraltech.smog.examples.model.generics.Box;

import static com.mistraltech.smog.proxy.javassist.JavassistMatcherGenerator.matcherOf;

public class MatcherFactory {
    @SuppressWarnings("unchecked")
    public static <P> BoxMatcher<P, ?, Box<P>> aBoxThat() {
        return matcherOf(BoxMatcher.class);
    }

    @SuppressWarnings("unchecked")
    public static <P1, P2> LabelledBoxMatcher<P1, P2> aLabelledBoxThat() {
        return matcherOf(LabelledBoxMatcher.class);
    }
}
