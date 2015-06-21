package com.mistraltech.smog.proxy.javassist;

import com.mistraltech.smog.core.CompositePropertyMatcher;
import com.mistraltech.smog.core.PropertyMatcher;
import com.mistraltech.smog.core.ReflectingPropertyMatcher;
import com.mistraltech.smog.core.util.PropertyDescriptorLocator;
import com.mistraltech.smog.proxy.javassist.util.JavaReflectionUtils;
import com.mistraltech.smog.proxy.javassist.util.JavassistClassUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import org.hamcrest.Matcher;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates a SMOG {@link CompositePropertyMatcher} implementation for a given matcher interface.
 * <p/>
 * The supplied matcher interface is required to have a {@link Matcher} annotation.
 * <p/>
 * The interface should declare methods beginning "has" for each relevant
 * property on the matched class e.g. hasName(..). These methods should take a single parameter
 * of the type of the property. There is no requirement to supply a "has" method for all properties.
 * <p/>
 * Additionally (or alternatively) a "has" method can be declared for each relevant property that takes
 * a single parameter of type Matcher<? super propertyType>, where propertyType is the type of the matched
 * property.
 * <p/>
 * The interface can also optionally declare a "like" method that takes an instance
 * of the matched class or a super-type and will use it to pre-populate the property matchers with expected values.
 */
public class JavassistMatcherGeneratorImpl {

    /**
     * Generate a matcher class instance for the specified interface.
     *
     * @param matcherInterface an interface for a matcher
     * @param <TM> the type of matcher to be generated
     * @return the new matcher instance
     */
    public static <TM> TM matcherOf(Class<TM> matcherInterface) {
        Class<TM> matcherClass = getMatcherClass(matcherInterface);
        return JavaReflectionUtils.createInstance(matcherClass);
    }

    /**
     * Get the matcher implementation class for the given matcher interface.
     *
     * @param matcherInterface the matcher interface
     * @param <TM> the type of the matcher interface
     * @return a class that implements the matcher
     */
    @SuppressWarnings("unchecked")
    private static <TM> Class<TM> getMatcherClass(Class<TM> matcherInterface) {
        final String matcherClassName = generateMatcherClassName(matcherInterface);

        try {
            return (Class<TM>) Class.forName(matcherClassName);
        } catch (ClassNotFoundException e) {
            return generateMatcherClass(matcherInterface, matcherClassName);
        }
    }

    private static <TM> String generateMatcherClassName(Class<TM> matcherInterface) {
        return matcherInterface.getName() + "SmogMatcher";
    }

    private static <TM> Class<TM> generateMatcherClass(Class<TM> matcherInterface, String matcherClassName) {
        final MatcherInterfaceWrapper<TM> matcherInterfaceWrapper = new MatcherInterfaceWrapper<TM>(matcherInterface);
        return generateMatcherClass(matcherInterfaceWrapper, matcherClassName);
    }

    private static <TM> Class<TM> generateMatcherClass(MatcherInterfaceWrapper<TM> matcherInterface, String matcherClassName) {
        final CtClass matcherCtSuperClass = JavassistClassUtils.getCtClass(CompositePropertyMatcher.class.getName());
        final CtClass matcherCtClass = buildMatcherCtClass(matcherClassName, matcherInterface, matcherCtSuperClass);

        final Class<TM> matcherClass = JavassistClassUtils.getClassFrom(matcherCtClass);
        matcherCtClass.detach();

        return matcherClass;
    }

    private static <TM> CtClass buildMatcherCtClass(String matcherClassName, MatcherInterfaceWrapper<TM> matcherInterface,
                                                    CtClass matcherCtSuperClass) {
        final CtClass generatedClass = ClassPool.getDefault().makeClass(matcherClassName, matcherCtSuperClass);
        generatedClass.addInterface(matcherInterface.getCtInterface());

        generateConstructor(matcherInterface, generatedClass);

        for (MatcherMethodWrapper matcherMethodDecl : matcherInterface.getMatcherMethods()) {
            generateMatcherMethod(generatedClass, matcherMethodDecl);
        }

        generateLikeMethod(matcherInterface, generatedClass);

        generateMatchesSafelyMethod(matcherInterface, generatedClass);

        return generatedClass;
    }

    private static void generateMatcherMethod(CtClass generatedClass, MatcherMethodWrapper matcherMethodDecl) {
        final String propertyName = matcherMethodDecl.getMatchedPropertyName();

        final String propertyMatcherFieldName = propertyName + "Matcher";
        final String propertyMatcherTypeName = PropertyMatcher.class.getName();
        final String reflectingPropertyMatcherTypeName = ReflectingPropertyMatcher.class.getName();
        final CtClass propertyMatcherCtClass = JavassistClassUtils.getCtClass(propertyMatcherTypeName);

        final String fieldInitializer = String.format("new %s(\"%s\", this)", reflectingPropertyMatcherTypeName, propertyName);

        if (!JavassistClassUtils.hasField(generatedClass, propertyMatcherFieldName)) {
            JavassistClassUtils.addField(generatedClass, propertyMatcherCtClass, propertyMatcherFieldName, fieldInitializer);
        }

        final String methodBody = generateMatcherMethodBody(propertyMatcherFieldName, matcherMethodDecl);

        JavassistClassUtils.addMethod(generatedClass, Modifier.PUBLIC, matcherMethodDecl.getCtMethod(), methodBody);
    }

    private static String generateMatcherMethodBody(String propertyMatcherName, MatcherMethodWrapper matcherMethod) {
        if (matcherMethod.takesHamcrestMatcher()) {
            return String.format("{ this.%s.setMatcher($1); return this; }", propertyMatcherName);
        } else {
            return String.format("{ this.%s.setMatcher(org.hamcrest.CoreMatchers.equalTo(($w)$1)); return this; }", propertyMatcherName);
        }
    }

    private static void generateConstructor(MatcherInterfaceWrapper<?> matcherInterface, CtClass matcherCtClass) {
        final String constructorBody = String.format("{ super(\"%s\"); }", matcherInterface.getMatchedClassDescription());
        JavassistClassUtils.addConstructor(matcherCtClass, constructorBody);
    }

    private static void generateLikeMethod(MatcherInterfaceWrapper<?> matcherInterface, CtClass matcherCtClass) {
        List<CtMethod> likeMethods = matcherInterface.getLikeMethods();

        for (CtMethod likeMethod : likeMethods) {
            final StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("{\n");

            final CtClass likeMethodParameterCtClass = JavassistClassUtils.getSingleParameterType(likeMethod);
            final Class<?> likeMethodParameterClass = JavassistClassUtils.getLoadedClass(likeMethodParameterCtClass);

            PropertyDescriptorLocator propertyDescriptorHelper = new PropertyDescriptorLocator(likeMethodParameterClass);

            Set<String> matchedProperties = new HashSet<String>();
            for (MatcherMethodWrapper matcherMethod : matcherInterface.getMatcherMethods()) {
                final String propertyName = matcherMethod.getMatchedPropertyName();

                if (!matchedProperties.contains(propertyName)) {
                    final PropertyDescriptor propertyDescriptor = propertyDescriptorHelper.findPropertyDescriptor(propertyName);

                    if (propertyDescriptor != null) {
                        final Method propertyReadMethod = propertyDescriptor.getReadMethod();
                        final String parameter = "$1." + propertyReadMethod.getName() + "()";

                        final CtClass propertyCtClass = JavassistClassUtils.getCtClass(propertyReadMethod.getReturnType().getName());
                        final CtClass matcherMethodParameterCtClass = matcherMethod.getParameterType();

                        if (matcherMethodParameterCtClass.equals(propertyCtClass)) {
                            // This matcher method takes the matched property type directly
                            // We have a winner...
                            matchedProperties.add(propertyName);
                            bodyBuilder.append(matcherMethod.getName()).append("(").append(parameter).append(");\n");
                        }
                    }
                }
            }

            bodyBuilder.append("return this;\n");
            bodyBuilder.append("}\n");
            String body = bodyBuilder.toString();

            JavassistClassUtils.addMethod(matcherCtClass, Modifier.PUBLIC, likeMethod, body);
        }
    }

    private static void generateMatchesSafelyMethod(MatcherInterfaceWrapper<?> matcherInterface, CtClass generatedClass) {
        final CtClass matchAccumulatorCtClass = SmogTypes.getMatchAccumulatorCtClass();
        final CtClass matchedCtClass = matcherInterface.getMatchedClass();

        final CtClass[] parameters = new CtClass[]{matchedCtClass, matchAccumulatorCtClass};

        JavassistClassUtils.addMethod(generatedClass, Modifier.PROTECTED, "matchesSafely", parameters,
                "{ super.matchesSafely($1, $2); }", CtClass.voidType);
    }
}
