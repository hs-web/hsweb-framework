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

/**
 * 权限设置
 *
 * @author hsweb-generator-online
 */
public class SimpleAuthorizationSettingEntity extends SimpleGenericEntity<String> implements AuthorizationSettingEntity {
    //类型
    private String type;
    //设置给谁
    private String settingFor;
    //状态
    private Byte   status;
    //备注
    private String describe;

    private List<AuthorizationSettingMenuEntity> menus;

    private List<AuthorizationSettingDetailEntity> details;

    @Override
    public List<AuthorizationSettingDetailEntity> getDetails() {
        return details;
    }

    @Override
    public void setDetails(List<AuthorizationSettingDetailEntity> details) {
        this.details = details;
    }

    @Override
    public List<AuthorizationSettingMenuEntity> getMenus() {
        return menus;
    }

    @Override
    public void setMenus(List<AuthorizationSettingMenuEntity> menus) {
        this.menus = menus;
    }

    /**
     * @return 类型
     */
    public String getType() {
        return this.type;
    }

    /**
     * 设置 类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return 设置给谁
     */
    public String getSettingFor() {
        return this.settingFor;
    }

    /**
     * 设置 设置给谁
     */
    public void setSettingFor(String settingFor) {
        this.settingFor = settingFor;
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

    /**
     * @return 备注
     */
    public String getDescribe() {
        return this.describe;
    }

    /**
     * 设置 备注
     */
    public void setDescribe(String describe) {
        this.describe = describe;
    }
}