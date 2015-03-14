package com.mistraltech.smog.examples.proxied;

import com.mistraltech.smog.examples.model.Address;
import com.mistraltech.smog.examples.model.Person;
import com.mistraltech.smog.examples.model.Phone;
import com.mistraltech.smog.examples.model.PostCode;
import com.mistraltech.smog.examples.proxied.matcher.PersonMatcher;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Test;

import static com.mistraltech.smog.examples.utils.MatcherTestUtils.assertMismatch;
import static com.mistraltech.smog.proxy.javassist.JavassistMatcherGeneratorImpl.matcherOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ProxiedMatcherExamplesTest {
    @Test
    public void testSimpleMatcherSucceedsWhenMatches() {
        Person input = new Person("bob", 36, new Address(21, new PostCode("out", "in")));
        assertThat(input, is(aPersonThat().hasAge(36).hasPhoneList(IsEmptyCollection.<Phone>empty())));
    }

    @Test
    public void testSimpleMatcherFailsWhenMismatches() {
        Person input = new Person("bob", 36, new Address(21, new PostCode("out", "in")));
        assertMismatch(input, is(aPersonThat().hasAge(35).hasPhoneList(IsEmptyCollection.<Phone>empty())), "age was <36> (expected <35>)");
    }

    private PersonMatcher aPersonThat() {
        return matcherOf(PersonMatcher.class);
    }
}
