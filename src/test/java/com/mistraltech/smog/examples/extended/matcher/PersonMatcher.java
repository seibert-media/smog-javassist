package com.mistraltech.smog.examples.extended.matcher;

import com.mistraltech.smog.core.annotation.Matches;
import com.mistraltech.smog.examples.model.Person;
import com.mistraltech.smog.examples.model.Phone;
import org.hamcrest.Matcher;

import java.util.List;

@Matches(value = Person.class, description = "a Person")
public interface PersonMatcher<R extends PersonMatcher<R, T>, T extends Person> extends AddresseeMatcher<R, T> {
    R like(Person p);

    R hasAge(int age);

    R hasAge(Matcher<? super Integer> ageMatcher);

    R hasPhoneList(Matcher<? super List<? extends Phone>> phoneListMatcher);
}
