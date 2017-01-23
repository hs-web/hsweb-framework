/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.entity.explorer;

import org.hswebframework.web.commons.entity.CloneableEntity;
import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;
import org.hswebframework.web.entity.authorization.SimpleActionEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleMenuEntity extends SimpleTreeSortSupportEntity<String>
        implements MenuEntity<SimpleMenuEntity, SimpleActionEntity> {

    //菜单名称
    private String name;

    //备注
    private String describe;

    //权限ID
    private String permissionId;

    //菜单对应的url
    private String url;

    //图标
    private String icon;

    //授权方式
    private String authentication;

    //授权配置
    private Map<String, Object> authenticationConfig;

    //菜单初始化时,执行脚本
    private String onInit;

    //是否启用
    private boolean enabled = true;

    //子菜单
    private List<SimpleMenuEntity> children;

    //可选操作(按钮)
    private List<SimpleActionEntity> actions;

    public List<SimpleActionEntity> getActions() {
        return actions;
    }

    public void setActions(List<SimpleActionEntity> actions) {
        this.actions = actions;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SimpleMenuEntity> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<SimpleMenuEntity> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public Map<String, Object> getAuthenticationConfig() {
        return authenticationConfig;
    }

    public void setAuthenticationConfig(Map<String, Object> authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }

    public String getOnInit() {
        return onInit;
    }

    public void setOnInit(String onInit) {
        this.onInit = onInit;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public SimpleMenuEntity clone() {
        SimpleMenuEntity target = new SimpleMenuEntity();
        target.setId(getId());
        target.setName(getName());
        target.setDescribe(getDescribe());
        target.setEnabled(isEnabled());
        target.setPermissionId(getPermissionId());
        target.setOnInit(getOnInit());
        target.setAuthentication(getAuthentication());
        target.setUrl(getUrl());
        target.setIcon(getIcon());
        target.setProperties(cloneProperties());
        target.setParentId(getParentId());
        target.setTreeCode(getTreeCode());
        target.setSortIndex(getSortIndex());
        if (null != getAuthenticationConfig()) {
            target.setAuthenticationConfig(getAuthenticationConfig()
                    .entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> {
                                if (e.getValue() instanceof CloneableEntity) {
                                    return ((CloneableEntity) e.getValue()).clone();
                                }
                                return e.getValue();
                            })));
        }
        if (null != getActions()) {
            target.setActions(getActions().stream().map(SimpleActionEntity::clone).collect(Collectors.toList()));
        }
        if (null != getChildren()) {
            target.setChildren(getChildren().stream().map(SimpleMenuEntity::clone).collect(Collectors.toList()));
        }
        return target;
    }
}
