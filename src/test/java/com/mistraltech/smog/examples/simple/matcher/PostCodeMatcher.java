package com.mistraltech.smog.examples.simple.matcher;

import com.mistraltech.smog.core.annotation.Matches;
import com.mistraltech.smog.examples.model.PostCode;
import org.hamcrest.Matcher;

@Matches(value = PostCode.class, description = "a Postcode")
public interface PostCodeMatcher extends Matcher<PostCode> {
    PostCodeMatcher hasInner(String inner);

    PostCodeMatcher hasInner(Matcher<? super String> innerMatcher);

    PostCodeMatcher hasOuter(String outer);

    PostCodeMatcher hasOuter(Matcher<? super String> outerMatcher);
}
