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

import org.hswebframework.web.commons.entity.TreeSortSupportEntity;

import java.util.List;

/**
 * 菜单分组 实体
 *
 * @author hsweb-generator-online
 */
public interface MenuGroupEntity extends TreeSortSupportEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 分组名称
     */
    String name         = "name";
    /**
     * 分组描述
     */
    String describe     = "describe";
    /**
     * 是否默认
     */
    String defaultGroup = "defaultGroup";
    /**
     * 树结构编码
     */
    String path         = "path";
    /**
     * 父级id
     */
    String parentId     = "parentId";
    /**
     * 树层级
     */
    String level        = "level";
    /**
     * 排序序号
     */
    String sortIndex    = "sortIndex";
    /**
     * 状态
     */
    String status       = "status";

    /**
     * @return 分组名称
     */
    String getName();

    /**
     * 设置 分组名称
     */
    void setName(String name);

    /**
     * @return 分组描述
     */
    String getDescribe();

    /**
     * 设置 分组描述
     */
    void setDescribe(String describe);

    /**
     * @return 是否默认
     */
    Boolean isDefaultGroup();

    /**
     * 设置 是否默认
     */
    void setDefaultGroup(Boolean defaultGroup);

    Byte getStatus();

    void setStatus(Byte status);

    List<MenuGroupBindEntity> getBindInfo();

    void setBindInfo(List<MenuGroupBindEntity> bindInfo);
}