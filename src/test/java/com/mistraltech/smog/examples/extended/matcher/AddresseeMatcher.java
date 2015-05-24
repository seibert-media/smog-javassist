package com.mistraltech.smog.examples.extended.matcher;

import com.mistraltech.smog.core.annotation.Matches;
import com.mistraltech.smog.examples.model.Address;
import com.mistraltech.smog.examples.model.Addressee;
import org.hamcrest.Matcher;

@Matches(value = Addressee.class, description = "an Addressee")
public interface AddresseeMatcher<R extends AddresseeMatcher<R, T>, T extends Addressee> extends Matcher<T> {
    R hasName(String name);

    R hasName(Matcher<? super String> nameMatcher);

    R hasAddress(Address address);

    R hasAddress(Matcher<? super Address> addressMatcher);

    R like(Addressee addressee);
}
