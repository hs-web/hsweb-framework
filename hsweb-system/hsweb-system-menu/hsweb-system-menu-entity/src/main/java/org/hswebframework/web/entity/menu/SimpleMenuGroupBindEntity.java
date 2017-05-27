/*
 *  Copyright 2016 http://www.hswebframework.org
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package org.hswebframework.web.entity.menu;

import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;
import org.hswebframework.web.entity.authorization.DataAccessEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单分组关联
 *
 * @author hsweb-generator-online
 */
public class SimpleMenuGroupBindEntity extends SimpleTreeSortSupportEntity<String> implements MenuGroupBindEntity {
    //是否启用
    private Boolean                           enabled;
    //菜单id
    private String                            menuId;
    //分组id
    private String                            groupId;
    //可选按钮
    private java.util.List<String>            actions;
    //行级权限控制配置
    private java.util.List<DataAccessEntity>  dataAccesses;
    //子节点
    private List<SimpleMenuGroupBindEntity>   children;

    /**
     * @return 是否启用
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * 设置 是否启用
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return 菜单id
     */
    public String getMenuId() {
        return this.menuId;
    }

    /**
     * 设置 菜单id
     */
    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    /**
     * @return 分组id
     */
    public String getGroupId() {
        return this.groupId;
    }

    /**
     * 设置 分组id
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * @return 可选按钮
     */
    public java.util.List<String> getActions() {
        return this.actions;
    }

    /**
     * 设置 可选按钮
     */
    public void setActions(java.util.List<String> actions) {
        this.actions = actions;
    }

    /**
     * @return 行级权限控制配置
     */
    public java.util.List<DataAccessEntity> getDataAccesses() {
        return this.dataAccesses;
    }

    /**
     * 设置 行级权限控制配置
     */
    public void setDataAccesses(java.util.List<DataAccessEntity> dataAccesses) {
        this.dataAccesses = dataAccesses;
    }


    public List<SimpleMenuGroupBindEntity> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<MenuGroupBindEntity> children) {
        this.children = new ArrayList(children);
    }
}