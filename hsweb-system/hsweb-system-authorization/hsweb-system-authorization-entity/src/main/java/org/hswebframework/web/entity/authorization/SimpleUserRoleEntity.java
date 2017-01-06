package org.hswebframework.web.entity.authorization;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleUserRoleEntity implements UserRoleEntity {

    private String userId;

    private String roleId;

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getRoleId() {
        return roleId;
    }

    @Override
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    @Override
    public SimpleUserRoleEntity clone() {
        SimpleUserRoleEntity target = new SimpleUserRoleEntity();
        target.setRoleId(getRoleId());
        target.setUserId(getUserId());
        return target;
    }
}
