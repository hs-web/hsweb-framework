package org.hsweb.web.bean.po.role;

import org.hsweb.web.bean.po.GenericPo;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 后台管理角色
 * Created by generator
 */
public class Role extends GenericPo<String> {

    public static final String SYS_ROLE_ADMIN = "admin";
    private static final long serialVersionUID = 9197157871004374522L;

    //角色名称
    @NotNull
    @NotEmpty(message = "名称不能为空")
    private String name;

    //备注
    private String remark;

    //角色类型
    private String type;

    //角色持有的模块
    private List<RoleModule> modules;

    public List<RoleModule> getModules() {
        return modules;
    }

    public void setModules(List<RoleModule> modules) {
        this.modules = modules;
    }


    /**
     * 获取 角色名称
     *
     * @return String 角色名称
     */
    public String getName() {
        if (this.name == null)
            return "";
        return this.name;
    }

    /**
     * 设置 角色名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取 备注
     *
     * @return String 备注
     */
    public String getRemark() {
        if (this.remark == null)
            return "";
        return this.remark;
    }

    /**
     * 设置 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取 角色类型
     *
     * @return String 角色类型
     */
    public String getType() {
        if (this.type == null)
            return "";
        return this.type;
    }

    /**
     * 设置 角色类型
     */
    public void setType(String type) {
        this.type = type;
    }
}
