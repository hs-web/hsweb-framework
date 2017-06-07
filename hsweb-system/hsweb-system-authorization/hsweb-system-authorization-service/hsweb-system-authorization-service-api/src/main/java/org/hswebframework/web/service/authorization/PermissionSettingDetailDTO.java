package org.hswebframework.web.service.authorization;

import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.entity.authorization.DataAccessEntity;

import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class PermissionSettingDetailDTO implements Entity {
    private String permissionId;

    private Set<String> actions;

    private Set<DataAccessEntity> dataAccesses;

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    public Set<String> getActions() {
        return actions;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    public Set<DataAccessEntity> getDataAccesses() {
        return dataAccesses;
    }

    public void setDataAccesses(Set<DataAccessEntity> dataAccesses) {
        this.dataAccesses = dataAccesses;
    }

}
