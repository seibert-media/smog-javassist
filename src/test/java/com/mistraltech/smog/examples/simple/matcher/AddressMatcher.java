package com.mistraltech.smog.examples.simple.matcher;

import com.mistraltech.smog.core.annotation.Matches;
import com.mistraltech.smog.examples.model.Address;
import com.mistraltech.smog.examples.model.PostCode;
import org.hamcrest.Matcher;

@Matches(value = Address.class, description = "an Address")
public interface AddressMatcher extends Matcher<Address> {
    AddressMatcher hasHouseNumber(Integer houseNumber);

    AddressMatcher hasHouseNumber(Matcher<? super Integer> houseNumberMatcher);

    AddressMatcher hasPostCode(PostCode postCode);

    AddressMatcher hasPostCode(Matcher<? super PostCode> postCodeMatcher);
}
