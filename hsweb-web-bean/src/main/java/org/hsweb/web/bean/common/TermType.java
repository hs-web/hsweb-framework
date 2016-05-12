package org.hsweb.web.bean.common;

/**
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
    notlike,
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
     * =''
     */
    empty,
    /**
     * !=''
     */
    notempty,
    /**
     * is null
     */
    isnull,
    /**
     * not null
     */
    notnull,
    /**
     * between
     */
    btw,
    /**
     * not between
     */
    notbtw;

    public static TermType fromString(String str) {
        if (str == null || !str.contains("$")) {
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
