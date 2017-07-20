package org.hswebframework.web.authorization.access;

import java.util.Set;

/**
 * 对字段进行过滤操作配置,实现字段级别的权限控制
 *
 * @author zhouhao
 * @see DataAccessConfig
 * @see org.hswebframework.web.authorization.simple.SimpleFieldFilterDataAccessConfig
 */
public interface FieldFilterDataAccessConfig extends DataAccessConfig {
    Set<String> getFields();
}
