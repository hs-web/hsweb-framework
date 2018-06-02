package org.hswebframework.web.service.authorization;

import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory;
import org.hswebframework.web.entity.authorization.DataAccessEntity;

/**
 * 数据权限配置工厂,用户将动态数据权限配置转为权限框架需要的配置,便于实现自定义数据权限
 *
 * @author zhouhao
 * @since 3.0
 * @see DataAccessConfig
 * @see DataAccessConfigBuilderFactory
 */
public interface DataAccessFactory {
    DataAccessConfig create(DataAccessEntity entity);
}
