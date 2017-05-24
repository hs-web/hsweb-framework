package org.hswebframework.web.organizational.authorization.access;

import org.hswebframework.web.authorization.access.DataAccessConfig;

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
     * @see DataAccessType.ScopeType
     */
    DataAccessType.ScopeType getScopeType();

    /**
     * @return 自定义的控制范围, 仅在scopeType为CUSTOM的时候有效
     * @see DataAccessType.ScopeType#CUSTOM
     */
    Set<String> getScope();
}
