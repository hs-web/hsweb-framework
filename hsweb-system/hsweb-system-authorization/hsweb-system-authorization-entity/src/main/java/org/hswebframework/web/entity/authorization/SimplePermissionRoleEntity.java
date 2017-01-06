package org.hswebframework.web.entity.authorization;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimplePermissionRoleEntity implements PermissionRoleEntity {
    private String roleId;

    private String permissionId;

    private List<String> actions;

    @Override
    public String getRoleId() {
        return roleId;
    }

    @Override
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    @Override
    public String getPermissionId() {
        return permissionId;
    }

    @Override
    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    @Override
    public List<String> getActions() {
        return actions;
    }

    @Override
    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    @Override
    public SimplePermissionRoleEntity clone() {
        SimplePermissionRoleEntity target = new SimplePermissionRoleEntity();
        if (actions != null)
            target.setActions(new ArrayList<>(getActions()));
        target.setPermissionId(getPermissionId());
        target.setRoleId(getRoleId());
        return target;
    }
}
