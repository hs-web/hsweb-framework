package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.CloneableEntity;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface UserRoleEntity extends CloneableEntity {

    String getUserId();

    void setUserId(String userId);

    String getRoleId();

    void setRoleId(String roleId);
}
