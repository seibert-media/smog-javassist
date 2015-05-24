package com.mistraltech.smog.proxy.javassist.util;

import static java.lang.Character.toLowerCase;

/**
 * Utility methods for manipulating names.
 */
public final class NameUtils {

    private NameUtils() {
    }

    /**
     * Convert the first character of the name to lowercase.
     *
     * @param name name to be converted
     * @return the name with the first character in lowercase
     */
    public static String deCapitalise(String name) {
        if (name.isEmpty() || Character.isLowerCase(name.charAt(0))) {
            return name;
        }

        return String.valueOf(toLowerCase(name.charAt(0))) + (name.length() > 1 ? name.substring(1) : "");
    }

    /**
     * Strip off any of a list of prefixes and return the result. Only the first matching prefix is removed. It may be
     * that no prefix is stripped if none of the prefixes matches.
     *
     * @param name the input name
     * @param prefixes list of prefixes to strip off
     * @return the resulting name
     */
    public static String removePrefix(String name, String... prefixes) {
        for (String prefix : prefixes) {
            if (name.startsWith(prefix) && name.length() > prefix.length()) {
                return name.substring(prefix.length());
            }
        }

        return name;
    }
}
