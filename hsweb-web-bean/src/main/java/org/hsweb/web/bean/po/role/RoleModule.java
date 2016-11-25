package org.hsweb.web.bean.po.role;

import org.hibernate.validator.constraints.NotEmpty;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.bean.po.module.Module;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 系统模块角色绑定
 * Created by generator
 */
public class RoleModule extends GenericPo<String> {
    private static final long serialVersionUID = 8910856253780046561L;

    //模块主键
    @NotNull
    @NotEmpty
    private String moduleId;

    //角色主键
    @NotNull
    @NotEmpty
    private String roleId;

    private List<String> actions;

    private transient Module module;


    /**
     * 获取 模块主键
     *
     * @return String 模块主键
     */
    public String getModuleId() {
        if (this.moduleId == null)
            return "";
        return this.moduleId;
    }

    /**
     * 设置 模块主键
     */
    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
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

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }



public interface Property extends GenericPo.Property{
	/**
	 *
	 * @see RoleModule#moduleId
	 */
	String moduleId="moduleId";
	/**
	 *
	 * @see RoleModule#roleId
	 */
	String roleId="roleId";
	/**
	 *
	 * @see RoleModule#actions
	 */
	String actions="actions";
	/**
	 *
	 * @see RoleModule#module
	 */
	String module="module";
	}
}