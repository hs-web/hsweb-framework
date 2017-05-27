package org.hswebframework.web.authorization.access;


import static org.hswebframework.web.authorization.access.DataAccessConfig.DefaultType.FIELD_SCOPE;

/**
 * 范围数据权限控制配置,控制某个字段的值在范围内
 *
 * @author zhouhao
 * @see ScopeDataAccessConfig
 * @since 3.0
 */
public interface FieldScopeDataAccessConfig extends ScopeDataAccessConfig {
    /**
     * @return 字段信息
     */
    String getField();

    @Override
    default String getType() {
        return FIELD_SCOPE;
    }
}
