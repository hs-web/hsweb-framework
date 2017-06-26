package org.hswebframework.web.entity.authorization.bind;

import org.hswebframework.web.entity.authorization.SimplePermissionRoleEntity;
import org.hswebframework.web.entity.authorization.SimpleRoleEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleBindPermissionRoleEntity extends SimpleRoleEntity implements BindPermissionRoleEntity<SimplePermissionRoleEntity> {
    private List<SimplePermissionRoleEntity> permissions;

    @Override
    public List<SimplePermissionRoleEntity> getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(List<SimplePermissionRoleEntity> permissions) {
        this.permissions = permissions;
    }

    @Override
    public SimpleBindPermissionRoleEntity clone() {
        SimpleBindPermissionRoleEntity target = ((SimpleBindPermissionRoleEntity) super.clone());
//        target.setId(getId());
//        target.setName(getName());
//        target.setDescribe(getDescribe());
        if (permissions != null && !permissions.isEmpty()) {
            target.permissions = permissions.stream().map(SimplePermissionRoleEntity::clone).collect(Collectors.toList());
        }
        return target;
    }
}
