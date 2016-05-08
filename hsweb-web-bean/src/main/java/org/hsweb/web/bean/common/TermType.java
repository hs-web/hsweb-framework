package org.hsweb.web.bean.common;

/**
 *
 * Created by zhouhao on 16-5-9.
 */
public enum TermType {
    /**
     * ==
     */
    eq,
    /**
     * !=
     */
    not,
    /**
     * like
     */
    like,
    /**
     * >
     */
    gt,
    /**
     * <
     */
    lt,
    /**
     * in
     */
    in,
    /**
     * notin
     */
    notin,
    /**
     * is null
     */
    isnull,
    /**
     * not null
     */
    notnull;

    public static TermType fromString(String str) {
        if (!str.contains("$")) {
            return eq;
        } else {
            try {
                return valueOf(str.split("[\\$]")[1].toLowerCase());
            } catch (Exception e) {
                return eq;
            }
        }
    }
}
