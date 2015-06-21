package com.mistraltech.smog.proxy.javassist;

import com.mistraltech.smog.core.MatchAccumulator;
import com.mistraltech.smog.proxy.javassist.util.JavassistClassUtils;
import javassist.CtClass;
import org.hamcrest.Matcher;

public class SmogTypes {
    public static CtClass getMatchAccumulatorCtClass() {
        return JavassistClassUtils.getCtClass(MatchAccumulator.class.getName());
    }

    public static CtClass getHamcrestMatcherCtClass() {
        return JavassistClassUtils.getCtClass(Matcher.class.getName());
    }
}
