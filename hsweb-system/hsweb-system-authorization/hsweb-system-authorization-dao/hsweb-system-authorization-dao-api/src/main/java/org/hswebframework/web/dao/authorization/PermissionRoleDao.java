package org.hswebframework.web.dao.authorization;

import org.hswebframework.web.entity.authorization.PermissionRoleEntity;
import org.hswebframework.web.dao.Dao;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Deprecated
public interface PermissionRoleDao extends Dao {
    int insert(PermissionRoleEntity permissionRoleBean);

    List<PermissionRoleEntity> selectByRoleId(String roleId);

    List<PermissionRoleEntity> selectByRoleIdList(List<String> roleIds);

    List<PermissionRoleEntity> selectByPermissionId(String roleId);

    int deleteByRoleId(String roleId);

    int deleteByPermissionId(String permissionId);
}
