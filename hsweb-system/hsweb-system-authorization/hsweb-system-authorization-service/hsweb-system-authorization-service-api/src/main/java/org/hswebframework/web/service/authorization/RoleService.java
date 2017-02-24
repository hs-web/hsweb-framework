package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.PermissionRoleEntity;
import org.hswebframework.web.entity.authorization.RoleEntity;
import org.hswebframework.web.entity.authorization.bind.BindPermissionRoleEntity;
import org.hswebframework.web.service.CreateEntityService;
import org.hswebframework.web.service.QueryByEntityService;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface RoleService extends
        CreateEntityService<RoleEntity>,
        QueryByEntityService<RoleEntity> {

    <T extends PermissionRoleEntity> String insert(BindPermissionRoleEntity<T> roleEntity);

    <T extends PermissionRoleEntity> void updateByPrimaryKey(BindPermissionRoleEntity<T> roleEntity);

    boolean enable(String roleId);

    boolean disable(String roleId);

    <T extends PermissionRoleEntity> boolean update(BindPermissionRoleEntity<T> roleEntity);

    RoleEntity selectByPk(String roleId);

}
