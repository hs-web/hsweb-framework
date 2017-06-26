package org.hswebframework.web.authorization.access;

import java.util.Set;

/**
 * 范围数据权限控制配置
 *
 * @author zhouhao
 * @see DataAccessConfig
 * @since 3.0
 */
public interface ScopeDataAccessConfig extends DataAccessConfig {

    /**
     * @return 范围类型
     */
    String getScopeType();

    /**
     * @return 自定义的控制范围
     */
    Set<Object> getScope();
}
