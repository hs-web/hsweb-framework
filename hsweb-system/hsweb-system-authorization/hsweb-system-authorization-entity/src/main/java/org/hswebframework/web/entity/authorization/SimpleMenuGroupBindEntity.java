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
package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单分组关联
 *
 * @author hsweb-generator-online
 */
public class SimpleMenuGroupBindEntity extends SimpleTreeSortSupportEntity<String> implements MenuGroupBindEntity {
    //状态
    private Byte                      status;
    //菜单id
    private String                    menuId;
    //分组id
    private String                    groupId;
    //子节点
    private List<MenuGroupBindEntity> children;

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
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


    public List<MenuGroupBindEntity> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<MenuGroupBindEntity> children) {
        this.children = children;
    }
}