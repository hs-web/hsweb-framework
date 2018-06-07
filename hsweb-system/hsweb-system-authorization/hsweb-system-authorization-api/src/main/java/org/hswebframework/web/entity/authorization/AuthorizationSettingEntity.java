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

import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.validator.group.CreateGroup;

import java.util.List;

/**
 * 权限设置 实体
 *
 * @author hsweb-generator-online
 */
public interface AuthorizationSettingEntity extends GenericEntity<String> {
  /*-------------------------------------------
    |                属性名常量                |
    ===========================================*/
    /**
     * 设置类型,如: role
     *
     * @see org.hswebframework.web.service.authorization.AuthorizationSettingTypeSupplier
     */
    String type = "type";
    /**
     * 设置给谁,通常是{@link this#type}对应的id
     * @see org.hswebframework.web.service.authorization.AuthorizationSettingTypeSupplier
     */
    String settingFor = "settingFor";
    /**
     * 状态
     */
    String status = "status";
    /**
     * 备注
     */
    String describe = "describe";

    /**
     * @return 类型
     */
    @NotBlank(groups = CreateGroup.class)
    String getType();

    /**
     * 设置 类型
     */
    void setType(String type);

    /**
     * @return 设置给谁
     */
    @NotBlank(groups = CreateGroup.class)
    String getSettingFor();

    /**
     * 设置 设置给谁
     */
    void setSettingFor(String settingFor);

    /**
     * @return 状态
     */
    Byte getStatus();

    /**
     * 设置 状态
     */
    void setStatus(Byte status);

    /**
     * @return 备注
     */
    String getDescribe();

    /**
     * 设置 备注
     */
    void setDescribe(String describe);

    List<AuthorizationSettingDetailEntity> getDetails();

    void setDetails(List<AuthorizationSettingDetailEntity> details);

    List<AuthorizationSettingMenuEntity> getMenus();

    void setMenus(List<AuthorizationSettingMenuEntity> menus);
}