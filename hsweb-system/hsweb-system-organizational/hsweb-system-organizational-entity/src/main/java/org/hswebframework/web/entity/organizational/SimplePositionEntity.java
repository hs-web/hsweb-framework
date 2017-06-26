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
 * 职位
 *
 * @author hsweb-generator-online
 */
public class SimplePositionEntity extends SimpleTreeSortSupportEntity<String> implements PositionEntity {
    //职位名称
    private String       name;
    //部门id
    private String       departmentId;
    //持有的角色
    private List<String> roles;
    //备注
    private String       remark;

    private List<PositionEntity> children;

    @Override
    @SuppressWarnings("unchecked")
    public List<PositionEntity> getChildren() {
        return children;
    }

    public void setChildren(List<PositionEntity> children) {
        this.children = children;
    }

    /**
     * @return 职位名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置 职位名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return 部门id
     */
    public String getDepartmentId() {
        return this.departmentId;
    }

    /**
     * 设置 部门id
     */
    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    @Override
    public List<String> getRoles() {
        return roles;
    }

    @Override
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    /**
     * @return 备注
     */
    public String getRemark() {
        return this.remark;
    }

    /**
     * 设置 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }
}