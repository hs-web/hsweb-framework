package org.hswebframework.web.authorization.token;

/**
 * 异地登录模式
 */
public enum AllopatricLoginMode {
    /**
     * 如果用户已在其他地方登录，则拒绝登录
     */
    deny,
    /**
     * 可以登录,同一个用户可在不同的地点登录
     */
    allow,
    /**
     * 如果用户已在其他地方登录，则将已登录的用户踢下线
     */
    offlineOther
}
