package org.hswebframework.web.authorization.basic.web;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface AuthorizedToken extends ParsedToken {
    String getUserId();
}
