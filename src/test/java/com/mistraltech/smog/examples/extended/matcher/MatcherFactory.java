package com.mistraltech.smog.examples.extended.matcher;

import com.mistraltech.smog.examples.model.Addressee;
import com.mistraltech.smog.examples.model.Person;

import static com.mistraltech.smog.proxy.javassist.JavassistMatcherGeneratorImpl.matcherOf;

public class MatcherFactory {
    @SuppressWarnings("unchecked")
    public static AddresseeMatcher<?, Addressee> anAddresseeThat() {
        return matcherOf(AddresseeMatcher.class);
    }

    @SuppressWarnings("unchecked")
    public static AddresseeMatcher<?, Addressee> anAddresseeLike(Addressee addressee) {
        return matcherOf(AddresseeMatcher.class).like(addressee);
    }

    @SuppressWarnings("unchecked")
    public static PersonMatcher<?, Person> aPersonThat() {
        return matcherOf(PersonMatcher.class);
    }

    @SuppressWarnings("unchecked")
    public static PersonMatcher<?, Person> aPersonLike(Person person) {
        return matcherOf(PersonMatcher.class).like(person);
    }
}
