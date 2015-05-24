package com.mistraltech.smog.proxy.javassist;

import com.mistraltech.smog.core.CompositePropertyMatcher;
import com.mistraltech.smog.core.MatchAccumulator;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mistraltech.smog.proxy.javassist.util.NameUtils.deCapitalise;
import static com.mistraltech.smog.proxy.javassist.util.NameUtils.removePrefix;

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

    private static final String MATCHER_METHOD_PREFIX = "has";

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
        final MatchesAnnotationWrapper matchesAnnotation = new MatchesAnnotationWrapper(matcherInterface);

        final CtClass matcherCtInterface = JavassistClassUtils.getCtClass(matcherInterface.getName());
        final CtClass matcherCtSuperClass = JavassistClassUtils.getCtClass(CompositePropertyMatcher.class.getName());

        final CtClass matcherCtClass = buildMatcherCtClass(matcherClassName, matchesAnnotation, matcherCtInterface, matcherCtSuperClass);

        final Class<TM> matcherClass = JavassistClassUtils.getClassFrom(matcherCtClass);
        matcherCtClass.detach();

        return matcherClass;
    }

    private static CtClass buildMatcherCtClass(String matcherClassName, MatchesAnnotationWrapper matchesAnnotation,
                                               CtClass matcherCtInterface, CtClass matcherCtSuperClass) {

        final CtClass matcherCtClass = ClassPool.getDefault().makeClass(matcherClassName, matcherCtSuperClass);
        matcherCtClass.addInterface(matcherCtInterface);

        generateConstructor(matchesAnnotation.getMatchedClassDescription(), matcherCtClass);

        for (CtMethod ctMethod : getMatcherMethods(matcherCtInterface)) {
            generateMatcherMethod(matcherCtClass, ctMethod);
        }

        generateLikeMethod(matcherCtInterface, matchesAnnotation.getMatchedClass(), matcherCtClass);

        generateMatchesSafelyMethod(matchesAnnotation.getMatchedClass(), matcherCtClass);

        return matcherCtClass;
    }

    private static void generateMatcherMethod(CtClass matcherCtClass, CtMethod ctMethod) {
        final String propertyName = convertMatcherMethodNameToPropertyName(ctMethod.getName());
        final String propertyMatcherFieldName = propertyName + "Matcher";
        final String propertyMatcherTypeName = PropertyMatcher.class.getName();
        final String reflectingPropertyMatcherTypeName = ReflectingPropertyMatcher.class.getName();
        final CtClass propertyMatcherCtClass = JavassistClassUtils.getCtClass(propertyMatcherTypeName);

        final String fieldInitializer = String.format("new %s(\"%s\", this)", reflectingPropertyMatcherTypeName, propertyName);

        if (!JavassistClassUtils.hasField(matcherCtClass, propertyMatcherFieldName)) {
            JavassistClassUtils.addField(matcherCtClass, propertyMatcherCtClass, propertyMatcherFieldName, fieldInitializer);
        }

        final String methodBody = generateMatcherMethodBody(propertyMatcherFieldName, ctMethod);

        JavassistClassUtils.addMethod(matcherCtClass, Modifier.PUBLIC, ctMethod, methodBody);
    }

    private static String generateMatcherMethodBody(String propertyMatcherName, CtMethod matcherMethod) {
        final CtClass parameterType = getSingleParameterType(matcherMethod);

        if (isHamcrestMatcher(parameterType)) {
            return String.format("{ this.%s.setMatcher($1); return this; }", propertyMatcherName);
        } else {
            return String.format("{ this.%s.setMatcher(org.hamcrest.CoreMatchers.equalTo(($w)$1)); return this; }", propertyMatcherName);
        }
    }

    private static String convertMatcherMethodNameToPropertyName(String name) {
        if (!hasMatcherMethodName(name)) {
            throw new IllegalArgumentException(
                    String.format("Matcher method name '%s' was expected to start with prefix '%s'", name, MATCHER_METHOD_PREFIX));
        }

        return deCapitalise(removePrefix(name, MATCHER_METHOD_PREFIX));
    }

    private static void generateConstructor(String matchedClassDescription, CtClass matcherCtClass) {
        final String constructorBody = String.format("{ super(\"%s\"); }", matchedClassDescription);
        JavassistClassUtils.addConstructor(matcherCtClass, constructorBody);
    }

    private static void generateLikeMethod(CtClass matcherCtInterface, Class<?> matchedClass, CtClass matcherCtClass) {
        List<CtMethod> likeMethods = getLikeMethods(matcherCtInterface, matchedClass);

        for (CtMethod likeMethod : likeMethods) {
            final StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("{\n");

            final CtClass likeMethodParameterCtClass = getSingleParameterType(likeMethod);
            final Class<?> likeMethodParameterClass = JavassistClassUtils.getLoadedClass(likeMethodParameterCtClass);

            PropertyDescriptorLocator propertyDescriptorHelper = new PropertyDescriptorLocator(likeMethodParameterClass);

            Set<String> matchedProperties = new HashSet<String>();
            for (CtMethod matcherCtMethod : getMatcherMethods(matcherCtInterface)) {
                final String propertyName = convertMatcherMethodNameToPropertyName(matcherCtMethod.getName());

                if (!matchedProperties.contains(propertyName)) {
                    final PropertyDescriptor propertyDescriptor = propertyDescriptorHelper.findPropertyDescriptor(propertyName);

                    if (propertyDescriptor != null) {
                        final Method propertyReadMethod = propertyDescriptor.getReadMethod();
                        final String parameter = "$1." + propertyReadMethod.getName() + "()";

                        final CtClass propertyCtClass = JavassistClassUtils.getCtClass(propertyReadMethod.getReturnType().getName());
                        final CtClass matcherMethodParameterCtClass = getSingleParameterType(matcherCtMethod);

                        if (matcherMethodParameterCtClass.equals(propertyCtClass)) {
                            // This matcher method takes the matched property type directly
                            // We have a winner...
                            matchedProperties.add(propertyName);
                            bodyBuilder.append(matcherCtMethod.getName()).append("(").append(parameter).append(");\n");
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

    private static void generateMatchesSafelyMethod(Class<?> matchedClass, CtClass matcherCtClass) {
        final CtClass matchAccumulatorCtClass = JavassistClassUtils.getCtClass(MatchAccumulator.class.getName());
        final CtClass matchedCtClass = JavassistClassUtils.getCtClass(matchedClass.getName());
        final CtClass[] parameters = new CtClass[]{matchedCtClass, matchAccumulatorCtClass};
        JavassistClassUtils.addMethod(matcherCtClass, Modifier.PROTECTED, "matchesSafely", parameters, "{ super.matchesSafely($1, $2); }", CtClass.voidType);
    }

    private static List<CtMethod> getLikeMethods(CtClass matcherCtInterface, Class<?> matchedClass) {
        List<CtMethod> methods = new ArrayList<CtMethod>();

        CtClass matchedCtClass = JavassistClassUtils.getCtClass(matchedClass.getName());

        for (CtMethod method : JavassistClassUtils.getMethods(matcherCtInterface)) {
            if (hasLikeMethodSignature(method, matchedCtClass, matcherCtInterface)) {
                methods.add(method);
            }
        }

        return methods;
    }

    private static boolean isHamcrestMatcher(CtClass parameterType) {
        final CtClass hamcrestMatcherCtClass = JavassistClassUtils.getCtClass(Matcher.class.getName());
        return JavassistClassUtils.isSubTypeOf(parameterType, hamcrestMatcherCtClass);
    }

    private static CtClass getSingleParameterType(CtMethod ctMethod) {
        final CtClass[] parameterTypes = JavassistClassUtils.getParameterTypes(ctMethod);

        if (parameterTypes.length != 1) {
            throw new RuntimeException("Unexpected method signature for " + ctMethod.getName() +
                    " - expected 1 parameter but got " + parameterTypes.length);
        }

        return parameterTypes[0];
    }

    private static boolean hasLikeMethodSignature(CtMethod ctMethod, CtClass matchedCtClass, CtClass matcherCtClass) {
        final CtClass returnType = JavassistClassUtils.getReturnType(ctMethod);
        final CtClass hamcrestMatcherCtClass = JavassistClassUtils.getCtClass(Matcher.class.getName());
        final CtClass[] parameterTypes = JavassistClassUtils.getParameterTypes(ctMethod);
        return parameterTypes.length == 1 &&
                ctMethod.getName().equals("like") &&
                JavassistClassUtils.isTypeInBounds(returnType, matcherCtClass, hamcrestMatcherCtClass) &&
                JavassistClassUtils.isSubTypeOf(matchedCtClass, parameterTypes[0]);
    }

    private static List<CtMethod> getMatcherMethods(CtClass matcherCtInterface) {
        List<CtMethod> methods = new ArrayList<CtMethod>();

        for (CtMethod method : JavassistClassUtils.getMethods(matcherCtInterface)) {
            if (hasMatcherMethodSignature(method, matcherCtInterface)) {
                methods.add(method);
            }
        }

        return methods;
    }

    private static boolean hasMatcherMethodSignature(CtMethod ctMethod, CtClass matcherCtClass) {
        final CtClass returnType = JavassistClassUtils.getReturnType(ctMethod);
        final CtClass hamcrestMatcherCtClass = JavassistClassUtils.getCtClass(Matcher.class.getName());
        final CtClass[] parameterTypes = JavassistClassUtils.getParameterTypes(ctMethod);
        return parameterTypes.length == 1 &&
                hasMatcherMethodName(ctMethod.getName()) &&
                JavassistClassUtils.isTypeInBounds(returnType, matcherCtClass, hamcrestMatcherCtClass);
    }

    private static boolean hasMatcherMethodName(String name) {
        return name.startsWith(MATCHER_METHOD_PREFIX);
    }
}
