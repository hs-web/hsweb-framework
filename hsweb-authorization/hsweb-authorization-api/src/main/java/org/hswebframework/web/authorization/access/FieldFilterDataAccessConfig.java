package org.hswebframework.web.authorization.access;

import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface FieldFilterDataAccessConfig extends DataAccessConfig {
    Set<String> getFields();
}
