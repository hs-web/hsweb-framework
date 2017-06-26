package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.GenericEntity;

/**
 * 角色实体
 *
 * @author zhouhao
 */
public interface RoleEntity extends GenericEntity<String> {

    String name     = "name";
    String describe = "describe";
    String enabled  = "enabled";

    String getName();

    void setName(String name);

    String getDescribe();

    void setDescribe(String describe);

    void setEnabled(Boolean enabled);

    Boolean isEnabled();
}
