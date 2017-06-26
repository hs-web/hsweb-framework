package org.hswebframework.web;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class RegexUtils {
    static Set<Character> SPECIAL_WORDS = new HashSet<>(Arrays.asList('\\', '$', '(', ')', '*', '+', '.', '[', ']', '?', '^', '{', '}', '|'));

    public static String escape(String regex) {
        if (regex == null || regex.isEmpty()) return regex;
        char[] chars = regex.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if (SPECIAL_WORDS.contains(chars[i])) {
                builder.append('\\');
            }
            builder.append(chars[i]);
        }
        return builder.toString();
    }

}
