package org.hswebframework.web.authorization.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.EnumDict;

/**
 * 令牌状态
 */
@Getter
@AllArgsConstructor
public enum TokenState implements EnumDict<String> {
    /**
     * 正常，有效
     */
    @Deprecated
    effective("effective", "正常"),

    /**
     * 正常，有效
     */
    normal("normal","正常"),

    /**
     * 已被禁止访问
     */
    deny("deny", "已被禁止访问"),

    /**
     * 已过期
     */
    expired("expired", "用户未登录"),

    /**
     * 已被踢下线
     * @see AllopatricLoginMode#offlineOther
     */
    offline("offline", "用户已在其他地方登录"),

    /**
     * 锁定
     */
    lock("lock", "登录状态已被锁定");

    private String value;

    private String text;
}
