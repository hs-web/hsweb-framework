package org.hswebframework.web.crud.query;

import io.netty.util.concurrent.FastThreadLocal;

public class QueryHelperUtils {

    static final FastThreadLocal<StringBuilder> SHARE = new FastThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() throws Exception {
            return new StringBuilder();
        }
    };

    public static String toSnake(String col) {
        StringBuilder builder = SHARE.get();
        builder.setLength(0);
        for (int i = 0, len = col.length(); i < len; i++) {
            char c = col.charAt(i);
            if (Character.isUpperCase(c)) {
                builder.append('_').append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public static String toHump(String col) {
        StringBuilder builder = SHARE.get();
        builder.setLength(0);
        boolean hasUpper = false, hasLower = false;
        for (int i = 0, len = col.length(); i < len; i++) {
            char c = col.charAt(i);
            if (Character.isLowerCase(c)) {
                hasLower = true;
            }
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            }
            if (hasUpper && hasLower) {
                return col;
            }
            if (c == '_') {
                if (i == len - 1) {
                    builder.append('_');
                } else {
                    builder.append(Character.toUpperCase(col.charAt(++i)));
                }
            } else {
                builder.append(Character.toLowerCase(c));
            }
        }
        return builder.toString();

    }
}
