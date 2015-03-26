package com.mistraltech.smog.examples.generics.matcher;

import com.mistraltech.smog.core.annotation.Matches;
import com.mistraltech.smog.examples.model.generics.Box;
import org.hamcrest.Matcher;

@Matches(value = Box.class, description = "a Box")
public interface BoxMatcher<P1, R extends BoxMatcher<P1, R, T>, T extends Box<P1>> extends Matcher<T> {

    R hasContents(final P1 contents);

    R hasContents(Matcher<? super P1> contentsMatcher);
}
