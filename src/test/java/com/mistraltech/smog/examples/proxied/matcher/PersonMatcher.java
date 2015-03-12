package com.mistraltech.smog.examples.proxied.matcher;

import com.mistraltech.smog.core.annotation.Matches;
import com.mistraltech.smog.examples.model.Person;
import com.mistraltech.smog.examples.model.Phone;
import org.hamcrest.Matcher;

import java.util.List;

@Matches(value = Person.class, description = "a Person")
public interface PersonMatcher extends Matcher<Person> {
    PersonMatcher hasAge(int age);

    PersonMatcher hasAge(Matcher<? super Integer> age);

    PersonMatcher hasPhoneList(List<Phone> phoneList);

    PersonMatcher hasPhoneList(Matcher<? super List<? extends Phone>> phoneListMatcher);
}
