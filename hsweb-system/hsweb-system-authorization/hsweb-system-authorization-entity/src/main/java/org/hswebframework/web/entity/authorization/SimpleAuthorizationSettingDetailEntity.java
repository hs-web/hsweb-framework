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

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import java.util.List;
import java.util.Set;

/**
 * 权限设置详情
 *
 * @author hsweb-generator-online
 */
public class SimpleAuthorizationSettingDetailEntity extends SimpleGenericEntity<String> implements AuthorizationSettingDetailEntity {
    //权限id
    private String                 permissionId;
    //设置id
    private String                 settingId;
    //可操作类型
    private Set<String>            actions;
    //数据权限控制
    private List<DataAccessEntity> dataAccesses;
    //状态
    private Byte                   status;
    //优先级
    private Long                   priority;
    //是否合并
    private Boolean                merge;

    /**
     * @return 权限id
     */
    public String getPermissionId() {
        return this.permissionId;
    }

    /**
     * 设置 权限id
     */
    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    /**
     * @return 设置id
     */
    public String getSettingId() {
        return this.settingId;
    }

    /**
     * 设置 设置id
     */
    public void setSettingId(String settingId) {
        this.settingId = settingId;
    }

    /**
     * @return 可操作类型
     */
    public Set<String> getActions() {
        return this.actions;
    }

    /**
     * 设置 可操作类型
     */
    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    /**
     * @return 数据权限控制
     */
    public List<DataAccessEntity> getDataAccesses() {
        return this.dataAccesses;
    }

    /**
     * 设置 数据权限控制
     */
    public void setDataAccesses(List<DataAccessEntity> dataAccesses) {
        this.dataAccesses = dataAccesses;
    }

    /**
     * @return 状态
     */
    public Byte getStatus() {
        return this.status;
    }

    /**
     * 设置 状态
     */
    public void setStatus(Byte status) {
        this.status = status;
    }

    @Override
    public Long getPriority() {
        return priority;
    }

    @Override
    public void setPriority(Long priority) {
        this.priority = priority;
    }

    public Boolean isMerge() {
        return merge;
    }

    @Override
    public void setMerge(Boolean merge) {
        this.merge = merge;
    }
}