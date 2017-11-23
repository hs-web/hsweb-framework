package org.hswebframework.web.authorization.basic.web;

/**
 * 已完成认证的令牌,如果返回此令牌,将直接使用{@link this#getUserId()}来绑定用户信息
 *
 * @author zhouhao
 */
public interface AuthorizedToken extends ParsedToken {

    /**
     * @return 令牌绑定的用户id
     */
    String getUserId();

    /**
     * @return 令牌有效期，单位毫秒，-1为长期有效
     */
    default long getMaxInactiveInterval() {
        return -1;
    }

}
