package org.hswebframework.web.entity.authorization;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimplePermissionRoleEntity implements PermissionRoleEntity {
    private String roleId;

    private String permissionId;

    private List<String> actions;

    private List<DataAccessEntity> dataAccesses;

    private List<FieldAccessEntity> fieldAccesses;

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
    public List<DataAccessEntity> getDataAccesses() {
        return this.dataAccesses;
    }

    @Override
    public List<FieldAccessEntity> getFieldAccesses() {
        return this.fieldAccesses;
    }

    @Override
    public void setDataAccesses(List<DataAccessEntity> dataAccesses) {
        this.dataAccesses = dataAccesses;
    }

    @Override
    public void setFieldAccesses(List<FieldAccessEntity> fieldAccesses) {
        this.fieldAccesses = fieldAccesses;
    }

    @Override
    public SimplePermissionRoleEntity clone() {
        SimplePermissionRoleEntity target = new SimplePermissionRoleEntity();
        target.setPermissionId(getPermissionId());
        target.setRoleId(getRoleId());
        if (actions != null)
            target.setActions(new ArrayList<>(getActions()));
        if (dataAccesses != null)
            target.setDataAccesses(dataAccesses.stream().map(DataAccessEntity::clone).collect(Collectors.toList()));
        if (fieldAccesses != null)
            target.setFieldAccesses(fieldAccesses.stream().map(FieldAccessEntity::clone).collect(Collectors.toList()));
        return target;
    }
}
