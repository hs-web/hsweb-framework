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
package org.hswebframework.web.entity.organizational;

import org.hswebframework.web.commons.entity.TreeSortSupportEntity;

/**
 * 部门 实体
 *
 * @author hsweb-generator-online
 */
public interface DepartmentEntity extends TreeSortSupportEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 名称
     */
    String name      = "name";
    /**
     * 所在组织id
     */
    String orgid     = "orgid";
    /**
     * 部门编码
     */
    String code      = "code";
    /**
     * 父级id
     */
    String parentId  = "parentId";
    /**
     * 树结构编码
     */
    String treeCode  = "treeCode";
    /**
     * 排序序号
     */
    String sortIndex = "sortIndex";
    /**
     * 是否启用
     */
    String enabled   = "enabled";
    /**
     * 级别
     */
    String level     = "level";

    /**
     * @return 名称
     */
    String getName();

    /**
     * 设置 名称
     */
    void setName(String name);

    /**
     * @return 所在组织id
     */
    String getOrgId();

    /**
     * 设置 所在组织id
     */
    void setOrgId(String orgId);

    /**
     * @return 部门编码
     */
    String getCode();

    /**
     * 设置 部门编码
     */
    void setCode(String code);

    /**
     * @return 是否启用
     */
    Boolean isEnabled();

    /**
     * 设置 是否启用
     */
    void setEnabled(Boolean enabled);

}