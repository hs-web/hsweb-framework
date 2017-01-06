package org.hswebframework.web.entity.authorization;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface Authorization {

    UserReadEntity getUser();

    List<PermissionRoleReadEntity> getRoles();

    List<PermissionReadEntity<ActionEntity>> getPermissions();

}
