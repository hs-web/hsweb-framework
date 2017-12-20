package org.hswebframework.web.authorization.builder;

/**
 * 数据权限配置构造器工厂
 *
 * @author zhouhao
 */
public interface DataAccessConfigBuilderFactory {
    /**
     * @return 新建一个数据权限配置构造器工厂
     */
    DataAccessConfigBuilder create();
}
