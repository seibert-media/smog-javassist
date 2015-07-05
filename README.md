# smog-javassist
Javassist-based runtime code generator extension for Smog.
## Summary
Matcher classes based on the [SMOG Matcher library](https://github.com/mistraltechnologies/smog) are
typically boiler-plate code.

To avoid the burden of writing
these, static code generators such as the [Smogen IntelliJ plugin](https://github.com/mistraltechnologies/smoged) can
be used. However, this still results in a lot of code that is baggage in a project.

Smog-Javassist is an extension to SMOG that removes the need for boiler-plate code without
resorting to a reflection-oriented solution, so that the benefits of statically typed code are retained: type-safety
and IDE-supported code completion and refactorings. It allows matchers to be written as interfaces only, with the
implementation being generated at runtime using [Javassist](http://www.javassist.org "javassist.org").

## Usage Examples

Begin by creating an interface containing the matcher methods you want:

    @Matches(value = Person.class, description = "a Person")
    public interface PersonMatcher {
    
        PersonMatcher hasName(String name);
    
        PersonMatcher hasName(Matcher<? super String> nameMatcher);
    
        PersonMatcher hasAddress(Address address);
    
        PersonMatcher hasAddress(Matcher<? super Address> addressMatcher);
        
        PersonMatcher hasAge(int age);
    
        PersonMatcher hasAge(Matcher<? super Integer> ageMatcher);
    
        PersonMatcher hasPhoneList(Matcher<? super List<? extends Phone>> phoneListMatcher);
    }

The @Matches annotation is required. It tells the code generator what class the matcher accepts and how to describe an
instance of that class.

Then in your test, import the 'matcherOf' factory method and instantiate a matcher like this:

    import static com.mistraltech.smog.proxy.javassist.JavassistMatcherGenerator.matcherOf;
    
    ...
        Matcher<Person> pm = matcherOf(PersonMatcher.class);
    ...
    
More commonly, you would instantiate the matcher within a Hamcrest assertion, such as:

    assertThat(bob, matcherOf(PersonMatcher.class).hasName("Bob"));
    
The readability of this assertion can be further improved by writing a factory method for your matcher:

    import static com.mistraltech.smog.proxy.javassist.JavassistMatcherGenerator.matcherOf;
    
    public final class MatcherFactory {
        public static PersonMatcher aPersonThat() {
            return matcherOf(PersonMatcher.class);
        }
    }
    
Given the above factory method and the gratuitous use of Hamcrest's is() method, the above assertion can then
be rewritten as:

    assertThat(bob, is(aPersonThat()).hasName("Bob"));

Notice that the PersonMatcher interface shown previously provides overloaded functions for each property: one that takes
a value that is the same type as the matched property, and one that takes a Matcher. The code generator will
generate appropriate implementations for both signatures. This allows other Hamcrest matchers to be used as
parameters (including other SMOG matchers):

    import static org.hamcrest.CoreMatchers.startsWith;

    ...
        assertThat(bob, is(aPersonThat()).hasName(startsWith("B")));
    ...
    
For more involved usage examples, see the tests in the library source code tree.  