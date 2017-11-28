package org.hswebframework.web;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zhouhao
 */
public class RegexUtils {
    private static Set<Character> SPECIAL_WORDS = new HashSet<>(Arrays.asList('\\', '$', '(', ')', '*', '+', '.', '[', ']', '?', '^', '{', '}', '|'));

    public static String escape(String regex) {
        if (regex == null || regex.isEmpty()) {
            return regex;
        }
        char[] chars = regex.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char aChar : chars) {
            if (SPECIAL_WORDS.contains(aChar)) {
                builder.append('\\');
            }
            builder.append(aChar);
        }
        return builder.toString();
    }

}
