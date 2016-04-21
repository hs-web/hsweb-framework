package org.hsweb.web.bean.po.role;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.hibernate.validator.constraints.NotEmpty;
import org.hsweb.web.bean.po.GenericPo;
import org.hsweb.web.bean.po.module.Module;
import org.webbuilder.utils.common.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.LinkedList;
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
    private String module_id;

    //角色主键
    @NotNull
    @NotEmpty
    private String role_id;

    //权限级别
    private String o_level;

    private transient Module module;

    private List<String> levels;


    /**
     * 获取 模块主键
     *
     * @return String 模块主键
     */
    public String getModule_id() {
        if (this.module_id == null)
            return "";
        return this.module_id;
    }

    /**
     * 设置 模块主键
     */
    public void setModule_id(String module_id) {
        this.module_id = module_id;
    }

    /**
     * 获取 角色主键
     *
     * @return String 角色主键
     */
    public String getRole_id() {
        if (this.role_id == null)
            return "";
        return this.role_id;
    }

    /**
     * 设置 角色主键
     */
    public void setRole_id(String role_id) {
        this.role_id = role_id;
    }

    /**
     * 获取 权限级别
     *
     * @return String 权限级别
     */
    public String getO_level() {
        if (this.o_level == null)
            return "";
        return this.o_level;
    }

    /**
     * 设置 权限级别
     */
    public void setO_level(String o_level) {
        this.o_level = o_level;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public List<String> getLevels() {
        if (!StringUtils.isNullOrEmpty(getO_level())) {
            try {
                if (levels == null)
                    levels = JSON.parseObject(getO_level(), new TypeReference<List<String>>(){});
            } catch (Exception e) {
                levels = new LinkedList<>();
            }
        } else {
            levels = new LinkedList<>();
        }
        return levels;
    }

    public void setLevels(List<String> levels) {
        this.levels = levels;
    }
}
