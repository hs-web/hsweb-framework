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
import org.hswebframework.web.organizational.authorization.access.DistrictAttachEntity;
import org.hswebframework.web.organizational.authorization.access.OrgAttachEntity;

import java.util.List;

/**
 * 组织 实体
 *
 * @author hsweb-generator-online
 */
public interface OrganizationalEntity extends TreeSortSupportEntity<String>, DistrictAttachEntity, OrgAttachEntity {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 名称
     */
    String name          = "name";
    /**
     * 全称
     */
    String fullName      = "fullName";
    /**
     * 机构编码
     */
    String code          = "code";
    /**
     * 可选角色
     */
    String optionalRoles = "optionalRoles";
    /**
     * 上级机构id
     */
    String parentId      = "parentId";
    /**
     * 树定位码
     */
    String path          = "path";
    /**
     * 树结构编码
     */
    String sortIndex     = "sortIndex";
    /**
     * 状态
     */
    String status        = "status";
    /**
     * 级别
     */
    String level         = "level";

    /**
     * @return 名称
     */
    String getName();

    /**
     * 设置 名称
     */
    void setName(String name);

    /**
     * @return 全称
     */
    String getFullName();

    /**
     * 设置 全称
     */
    void setFullName(String fullName);

    /**
     * @return 机构编码
     */
    String getCode();

    /**
     * 设置 机构编码
     */
    void setCode(String code);

    /**
     * @return 可选角色
     */
    @Deprecated
    java.util.List<String> getOptionalRoles();

    /**
     * 设置 可选角色
     */
    @Deprecated
    void setOptionalRoles(java.util.List<String> optionalRoles);

    void setChildren(List<OrganizationalEntity> children);

    Byte getStatus();

    void setStatus(Byte status);

    @Override
    default String getOrgId() {
        return getId();
    }

    @Override
    default void setOrgId(String orgId) {
        setId(orgId);
    }
}