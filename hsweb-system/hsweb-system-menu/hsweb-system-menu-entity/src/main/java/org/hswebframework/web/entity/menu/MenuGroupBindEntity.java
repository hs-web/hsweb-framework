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

import org.hswebframework.web.commons.entity.TreeSortSupportEntity;
import org.hswebframework.web.entity.authorization.DataAccessEntity;
import org.hswebframework.web.entity.authorization.FieldAccessEntity;

import java.util.List;

/**
 * 菜单分组关联 实体
 *
 * @author hsweb-generator-online
 */
public interface MenuGroupBindEntity extends TreeSortSupportEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 树结构编码
     */
    String treeCode      = "treeCode";
    /**
     * 父级id
     */
    String parentId      = "parentId";
    /**
     * 树层级
     */
    String level         = "level";
    /**
     * 排序序号
     */
    String sortIndex     = "sortIndex";
    /**
     * 是否启用
     */
    String enable        = "enabled";
    /**
     * 菜单id
     */
    String menuId        = "menuId";
    /**
     * 分组id
     */
    String groupId       = "groupId";
    /**
     * 可选按钮
     */
    String actions       = "actions";
    /**
     * 行级权限控制配置
     */
    String dataAccesses  = "dataAccesses";
    /**
     * 列级权限控制
     */
    String fieldAccesses = "fieldAccesses";

    /**
     * @return 是否启用
     */
    Boolean isEnabled();

    /**
     * 设置 是否启用
     */
    void setEnabled(Boolean enabled);

    /**
     * @return 菜单id
     */
    String getMenuId();

    /**
     * 设置 菜单id
     */
    void setMenuId(String menuId);

    /**
     * @return 分组id
     */
    String getGroupId();

    /**
     * 设置 分组id
     */
    void setGroupId(String groupId);

    /**
     * @return 可选按钮
     */
    java.util.List<String> getActions();

    /**
     * 设置 可选按钮
     */
    void setActions(java.util.List<String> actions);

    /**
     * @return 行级权限控制配置
     */
    java.util.List<DataAccessEntity> getDataAccesses();

    /**
     * 设置 行级权限控制配置
     */
    void setDataAccesses(java.util.List<DataAccessEntity> dataAccesses);

    /**
     * @return 列级权限控制
     */
    java.util.List<FieldAccessEntity> getFieldAccesses();

    /**
     * 设置 列级权限控制
     */
    void setFieldAccesses(java.util.List<FieldAccessEntity> fieldAccesses);

    void setChildren(List<MenuGroupBindEntity> children);
}