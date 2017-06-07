package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.CloneableEntity;

import java.util.List;

/**
 * 权限设置已经重构至 {@link AuthorizationSettingEntity} ,将在接下来移除
 *
 * @author zhouhao
 */
@Deprecated
public interface PermissionRoleEntity extends CloneableEntity {

    void setRoleId(String roleId);

    void setPermissionId(String permissionId);

    void setActions(List<String> actions);

    String getRoleId();

    String getPermissionId();

    List<String> getActions();

    List<DataAccessEntity> getDataAccesses();

    void setDataAccesses(List<DataAccessEntity> dataAccesses);
}
