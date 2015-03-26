package com.mistraltech.smog.examples.generics.matcher;

import com.mistraltech.smog.core.annotation.Matches;
import com.mistraltech.smog.examples.model.generics.LabelledBox;
import org.hamcrest.Matcher;

@Matches(value = LabelledBox.class, description = "a LabelledBox")
public interface LabelledBoxMatcher<P1, P2> extends BoxMatcher<P1, LabelledBoxMatcher<P1, P2>, LabelledBox<P1, P2>> {
    LabelledBoxMatcher<P1, P2> hasLabel(final P2 label);

    LabelledBoxMatcher<P1, P2> hasLabel(Matcher<? super P2> labelMatcher);
}
