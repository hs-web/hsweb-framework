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

import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;

import java.util.List;

/**
 * 权限菜单
 *
 * @author hsweb-generator-online
 */
public class SimpleAuthorizationSettingMenuEntity extends SimpleTreeSortSupportEntity<String> implements AuthorizationSettingMenuEntity {
    //菜单id
    private String menuId;
    //设置id
    private String settingId;
    //状态
    private Byte   status;
    //其他配置内容
    private String config;

    private List<AuthorizationSettingMenuEntity> children;

    /**
     * @return 菜单id
     */
    public String getMenuId() {
        return this.menuId;
    }

    /**
     * 设置 菜单id
     */
    public void setMenuId(String menuId) {
        this.menuId = menuId;
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
     * @return 其他配置内容
     */
    public String getConfig() {
        return this.config;
    }

    /**
     * 设置 其他配置内容
     */
    public void setConfig(String config) {
        this.config = config;
    }

    @Override
    public List<AuthorizationSettingMenuEntity> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<AuthorizationSettingMenuEntity> children) {
        this.children = children;
    }
}