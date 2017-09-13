package org.hswebframework.web.authorization.basic.web;

/**
 * 已完成认证的令牌,如果返回此令牌,将直接使用{@link this#getUserId()}来绑定用户信息
 * @author zhouhao
 */
public interface AuthorizedToken extends ParsedToken {
    String getUserId();
}
