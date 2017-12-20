package org.hswebframework.web.authorization.builder;

/**
 * 权限构造器工厂
 *
 * @author zhouhao
 */
public interface AuthenticationBuilderFactory {
    /**
     * @return 新建一个权限构造器
     */
    AuthenticationBuilder create();
}
