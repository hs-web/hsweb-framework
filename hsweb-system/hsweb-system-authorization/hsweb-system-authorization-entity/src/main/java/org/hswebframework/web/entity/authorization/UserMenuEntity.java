package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class UserMenuEntity extends SimpleTreeSortSupportEntity<String> {

    private String menuId;

    //菜单名称
    private String name;

    //备注
    private String describe;

    //权限ID
    private String permissionId;

    //菜单对应的url
    private String url;

    //图标
    private String icon;

    private List<UserMenuEntity> children;

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public List<UserMenuEntity> getChildren() {
        return children;
    }

    public void setChildren(List<UserMenuEntity> children) {
        this.children = children;
    }
}
