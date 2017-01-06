package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.CloneableEntity;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface PermissionRoleEntity extends PermissionRoleReadEntity, CloneableEntity {

    void setRoleId(String roleId);

    void setPermissionId(String permissionId);

    void setActions(List<String> actions);

}
