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
    String status   = "status";

    String getName();

    void setName(String name);

    String getDescribe();

    void setDescribe(String describe);

    void setStatus(Byte status);

    Byte getStatus();
}
