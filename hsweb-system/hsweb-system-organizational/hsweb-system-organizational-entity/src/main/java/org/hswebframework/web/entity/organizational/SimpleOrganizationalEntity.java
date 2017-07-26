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

import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;

import java.util.List;

/**
 * 组织
 *
 * @author hsweb-generator-online
 */
public class SimpleOrganizationalEntity extends SimpleTreeSortSupportEntity<String> implements OrganizationalEntity {
    //名称
    private String                     name;
    //全称
    private String                     fullName;
    //机构编码
    private String                     code;
    //可选角色
    private java.util.List<String>     optionalRoles;
    //是否启用
    private Byte                       status;
    //子级组织
    private List<OrganizationalEntity> children;

    private String areaId;

    @Override
    public String getDistrictId() {
        return areaId;
    }

    @Override
    public void setDistrictId(String districtId) {
        this.areaId = districtId;
    }

    /**
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return 全称
     */
    public String getFullName() {
        return this.fullName;
    }

    /**
     * 设置 全称
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return 机构编码
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 设置 机构编码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return 可选角色
     */
    public java.util.List<String> getOptionalRoles() {
        return this.optionalRoles;
    }

    /**
     * 设置 可选角色
     */
    public void setOptionalRoles(java.util.List<String> optionalRoles) {
        this.optionalRoles = optionalRoles;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OrganizationalEntity> getChildren() {
        return children;
    }

    public void setChildren(List<OrganizationalEntity> children) {
        this.children = children;
    }

    @Override
    public Byte getStatus() {
        return status;
    }

    @Override
    public void setStatus(Byte status) {
        this.status = status;
    }
}