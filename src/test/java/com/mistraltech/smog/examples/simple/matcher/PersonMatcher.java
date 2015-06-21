package com.mistraltech.smog.examples.simple.matcher;

import com.mistraltech.smog.core.annotation.Matches;
import com.mistraltech.smog.core.annotation.MatchesProperty;
import com.mistraltech.smog.examples.model.Person;
import com.mistraltech.smog.examples.model.Phone;
import org.hamcrest.Matcher;

import java.util.List;

@Matches(value = Person.class, description = "a Person")
public interface PersonMatcher extends AddresseeMatcher<PersonMatcher, Person> {

    PersonMatcher hasAge(int yearsOld);

    PersonMatcher hasAge(Matcher<? super Integer> yearsOldMatcher);

    // Equivalent to hasAge...

    @MatchesProperty("age")
    PersonMatcher havingYearsOld(int yearsOld);

    @MatchesProperty("age")
    PersonMatcher havingYearsOld(Matcher<? super Integer> yearsOldMatcher);

    PersonMatcher hasPhoneList(Matcher<? super List<? extends Phone>> phoneListMatcher);
}
