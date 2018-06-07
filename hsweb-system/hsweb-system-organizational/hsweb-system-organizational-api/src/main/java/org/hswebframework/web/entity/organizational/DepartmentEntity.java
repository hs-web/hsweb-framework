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
import org.hswebframework.web.organizational.authorization.access.DepartmentAttachEntity;
import org.hswebframework.web.organizational.authorization.access.OrgAttachEntity;

import java.util.List;

/**
 * 部门 实体
 *
 * @author hsweb-generator-online
 */
public interface DepartmentEntity extends  TreeSortSupportEntity<String>, OrgAttachEntity, DepartmentAttachEntity {
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
    String orgId     = "orgId";
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
    String path      = "path";
    /**
     * 排序序号
     */
    String sortIndex = "sortIndex";
    /**
     * 状态
     */
    String status    = "status";
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
     * @return 部门编码
     */
    String getCode();

    /**
     * 设置 部门编码
     */
    void setCode(String code);

    /**
     * @return 状态
     */
    Byte getStatus();

    /**
     * 设置 状态
     */
    void setStatus(Byte status);

    void setChildren(List<DepartmentEntity> children);

    @Override
    default String getDepartmentId() {
        return getId();
    }

    @Override
    default void setDepartmentId(String departmentId) {
        setId(departmentId);
    }
}