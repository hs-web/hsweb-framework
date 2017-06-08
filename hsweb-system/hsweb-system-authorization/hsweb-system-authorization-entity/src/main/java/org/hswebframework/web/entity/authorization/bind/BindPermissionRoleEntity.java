package org.hswebframework.web.entity.authorization.bind;

import org.hswebframework.web.entity.authorization.PermissionRoleEntity;
import org.hswebframework.web.entity.authorization.RoleEntity;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Deprecated
public interface BindPermissionRoleEntity<T extends PermissionRoleEntity> extends RoleEntity {
    List<T> getPermissions();

    void setPermissions(List<T> permissions);
}
