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
import org.hswebframework.web.organizational.authorization.access.PositionAttachEntity;

import java.util.List;

/**
 * 职位 实体
 *
 * @author hsweb-generator-online
 */
public interface PositionEntity extends TreeSortSupportEntity<String>, DepartmentAttachEntity, PositionAttachEntity {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 职位名称
     */
    String name         = "name";
    /**
     * 部门id
     */
    String departmentId = "departmentId";
    /**
     * 持有的角色
     */
    String roles        = "roles";
    /**
     * 备注
     */
    String remark       = "remark";
    /**
     * 父级id
     */
    String parentId     = "parentId";
    /**
     * 树结构编码
     */
    String path         = "path";
    /**
     * 排序索引
     */
    String sortIndex    = "sortIndex";
    /**
     * 级别
     */
    String level        = "level";

    /**
     * @return 职位名称
     */
    String getName();

    /**
     * 设置 职位名称
     */
    void setName(String name);

    /**
     * @return 持有的角色
     */
    List<String> getRoles();

    /**
     * 设置 持有的角色
     */
    void setRoles(List<String> roles);

    /**
     * @return 备注
     */
    String getRemark();

    /**
     * 设置 备注
     */
    void setRemark(String remark);

    void setChildren(List<PositionEntity> children);

    @Override
    default String getPositionId() {
        return getId();
    }

    @Override
    default void setPositionId(String positionId) {
        setId(positionId);
    }
}