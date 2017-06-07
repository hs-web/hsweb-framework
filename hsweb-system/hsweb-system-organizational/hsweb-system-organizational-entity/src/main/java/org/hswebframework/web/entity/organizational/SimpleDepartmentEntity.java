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
 * 部门
 *
 * @author hsweb-generator-online
 */
public class SimpleDepartmentEntity extends SimpleTreeSortSupportEntity<String> implements DepartmentEntity {
    //名称
    private String name;
    //所在组织id
    private String orgId;
    //部门编码
    private String code;
    //是否启用
    private Byte   status;

    private List<DepartmentEntity> children;

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
     * @return 所在组织id
     */
    public String getOrgId() {
        return this.orgId;
    }

    /**
     * 设置 所在组织id
     */
    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    /**
     * @return 部门编码
     */
    public String getCode() {
        return this.code;
    }

    /**
     * 设置 部门编码
     */
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public Byte getStatus() {
        return status;
    }

    @Override
    public void setStatus(Byte status) {
        this.status = status;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DepartmentEntity> getChildren() {
        return children;
    }

    public void setChildren(List<DepartmentEntity> children) {
        this.children = children;
    }
}