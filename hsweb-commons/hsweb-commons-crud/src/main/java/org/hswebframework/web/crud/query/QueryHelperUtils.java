package org.hswebframework.web.crud.query;

import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.recycler.Recycler;
import org.hswebframework.web.recycler.Recyclers;

public class QueryHelperUtils {

    static final Recycler<StringBuilder> SHARE = Recyclers.STRING_BUILDER;

    public static String toSnake(String col) {
        return SHARE.doWith(col, (builder, _col) -> {
            for (int i = 0, len = _col.length(); i < len; i++) {
                char c = _col.charAt(i);
                if (Character.isUpperCase(c)) {
                    if (i != 0) {
                        builder.append('_');
                    }
                    builder.append(Character.toLowerCase(c));
                } else {
                    builder.append(c);
                }
            }
            return builder.toString();
        });
    }

    public static String toHump(String col) {
        return SHARE.doWith(col, (builder, _col) -> {
            boolean hasUpper = false, hasLower = false;
            for (int i = 0, len = _col.length(); i < len; i++) {
                char c = _col.charAt(i);
                if (Character.isLowerCase(c)) {
                    hasLower = true;
                }
                if (Character.isUpperCase(c)) {
                    hasUpper = true;
                }
                if (hasUpper && hasLower) {
                    return _col;
                }
                if (c == '_') {
                    if (i == len - 1) {
                        builder.append('_');
                    } else {
                        builder.append(Character.toUpperCase(_col.charAt(++i)));
                    }
                } else {
                    builder.append(Character.toLowerCase(c));
                }
            }
            return builder.toString();
        });
    }

    public static void assertLegalColumn(String col) {
        if (!isLegalColumn(col)) {
            throw new BusinessException.NoStackTrace("error.illegal_column_name", col);
        }
    }

    public static boolean isLegalColumn(String col) {
        int len = col.length();
        for (int i = 0; i < len; i++) {
            char c = col.charAt(i);
            if (c == '_' || c == '$' || Character.isLetterOrDigit(c)) {
                continue;
            }
            return false;
        }
        return true;
    }
}
