/*
 *  Copyright 2019 http://www.hswebframework.org
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

import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.validator.group.CreateGroup;

import java.util.List;
import java.util.Set;

/**
 * 权限设置详情 实体
 *
 * @author hsweb-generator-online
 */
public interface AuthorizationSettingDetailEntity extends GenericEntity<String>, Comparable<AuthorizationSettingDetailEntity> {

    Byte STATE_OK = 1;

   /*-------------------------------------------
    |               属性名常量                 |
    ===========================================*/
    /**
     * 权限id
     */
    String permissionId = "permissionId";
    /**
     * 设置id
     */
    String settingId    = "settingId";
    /**
     * 可操作类型
     */
    String actions      = "actions";
    /**
     * 数据权限控制
     */
    String dataAccesses = "dataAccesses";
    /**
     * 状态
     */
    String status        = "status";

    /**
     * @return 权限id
     */
    @NotBlank(groups = CreateGroup.class)
    String getPermissionId();

    /**
     * 设置 权限id
     */
    void setPermissionId(String permissionId);

    /**
     * @return 设置id
     */
    @NotBlank(groups = CreateGroup.class)
    String getSettingId();

    /**
     * 设置 设置id
     */
    void setSettingId(String settingId);

    /**
     * @return 可操作类型
     */
    Set<String> getActions();

    /**
     * 设置 可操作类型
     */
    void setActions(Set<String> actions);

    /**
     * @return 数据权限控制
     */
    List<DataAccessEntity> getDataAccesses();

    /**
     * 设置 数据权限控制
     */
    void setDataAccesses(List<DataAccessEntity> dataAccesses);

    /**
     * @return 状态
     */
    Byte getStatus();

    /**
     * 设置 状态
     */
    void setStatus(Byte status);

    Long getPriority();

    void setPriority(Long priority);

    Boolean getMerge();

    void setMerge(Boolean merge);

    @Override
    default int compareTo(AuthorizationSettingDetailEntity target) {
        return Long.compare(getPriority() == null ? 0 : getPriority(), target.getPriority() == null ? 0 : target.getPriority());
    }
}