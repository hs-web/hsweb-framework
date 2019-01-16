/*
 *  Copyright 2019 http://www.hswebframework.org
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
package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.TreeSortSupportEntity;

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
    String path      = "path";
    /**
     * 父级id
     */
    String parentId  = "parentId";
    /**
     * 树层级
     */
    String level     = "level";
    /**
     * 排序序号
     */
    String sortIndex = "sortIndex";
    /**
     * 状态
     */
    String status     = "status";
    /**
     * 菜单id
     */
    String menuId    = "menuId";
    /**
     * 分组id
     */
    String groupId   = "groupId";

    /**
     * @return 状态
     */
    Byte getStatus();

    /**
     * 设置 状态
     */
    void setStatus(Byte status);

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


    void setChildren(List<MenuGroupBindEntity> children);
}