package org.hswebframework.web.authorization.token;

/**
 * 令牌状态
 */
public enum TokenState {
    /**
     * 正常，有效
     */
    effective,

    /**
     * 已被禁止访问
     */
    deny,

    /**
     * 已过期
     */
    expired,

    /**
     * 已被踢下线
     */
    offline
}
