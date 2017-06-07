package org.hswebframework.web.service.authorization;

import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.entity.authorization.MenuEntity;
import org.hswebframework.web.entity.authorization.MenuGroupEntity;

import java.util.List;
import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class PermissionSettingDTO implements Entity {
    //配置类型,如 role,user,position,person等
    private String type;

    //配置给谁,为type对应数据的主键
    private String settingFor;

    private String describe;

    private List<PermissionSettingDetailDTO> details;

    private Set<MenuEntity> menus;

    private Set<MenuGroupEntity> menuGroups;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSettingFor() {
        return settingFor;
    }

    public void setSettingFor(String settingFor) {
        this.settingFor = settingFor;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public List<PermissionSettingDetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<PermissionSettingDetailDTO> details) {
        this.details = details;
    }

    public Set<MenuEntity> getMenus() {
        return menus;
    }

    public void setMenus(Set<MenuEntity> menus) {
        this.menus = menus;
    }

    public Set<MenuGroupEntity> getMenuGroups() {
        return menuGroups;
    }

    public void setMenuGroups(Set<MenuGroupEntity> menuGroups) {
        this.menuGroups = menuGroups;
    }
}
