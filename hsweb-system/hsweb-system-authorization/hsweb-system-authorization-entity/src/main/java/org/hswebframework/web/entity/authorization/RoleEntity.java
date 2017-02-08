package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.GenericEntity;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface RoleEntity extends GenericEntity<String> {

    String getName();

    void setName(String name);

    String getDescribe();

    void setDescribe(String describe);

    void setEnabled(Boolean enabled);

    Boolean isEnabled();
}
