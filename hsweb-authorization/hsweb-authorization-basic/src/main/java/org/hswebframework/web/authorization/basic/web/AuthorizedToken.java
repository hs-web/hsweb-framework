package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.token.ParsedToken;

/**
 * 已完成认证的令牌,如果返回此令牌,将直接使用{@link AuthorizedToken#getUserId()}来绑定用户信息
 *
 * @author zhouhao
 */
public interface AuthorizedToken extends ParsedToken {

    /**
     * @return 令牌绑定的用户id
     */
    String getUserId();

    /**
     * 获取认证权限信息
     *
     * @return Authentication
     * @since 4.0.17
     */
    default Authentication getAuthentication() {
        return null;
    }

    /**
     * @return 令牌有效期，单位毫秒，-1为长期有效
     */
    default long getMaxInactiveInterval() {
        return -1;
    }

}
