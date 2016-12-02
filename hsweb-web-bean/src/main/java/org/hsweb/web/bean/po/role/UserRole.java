package org.hsweb.web.bean.po.role;

import org.hibernate.validator.constraints.NotEmpty;
import org.hsweb.web.bean.po.GenericPo;

import javax.validation.constraints.NotNull;

/**
 * 后台管理用户角色绑定
 * Created by generator
 */
public class UserRole extends GenericPo<String> {
    private static final long serialVersionUID = 8910856253780046561L;

    //用户主键
    @NotEmpty
    private String userId;

    //角色主键
    @NotEmpty
    private String roleId;

    //角色实例
    private transient Role role;

    /**
     * 获取 用户主键
     *
     * @return String 用户主键
     */
    public String getUserId() {
        if (this.userId == null)
            return "";
        return this.userId;
    }

    /**
     * 设置 用户主键
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取 角色主键
     *
     * @return String 角色主键
     */
    public String getRoleId() {
        if (this.roleId == null)
            return "";
        return this.roleId;
    }

    /**
     * 设置 角色主键
     */
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }


    public interface Property {
        /**
         * @see UserRole#userId
         */
        String userId = "userId";
        /**
         * @see UserRole#roleId
         */
        String roleId = "roleId";
        /**
         * @see UserRole#role
         */
        String role   = "role";
    }
}