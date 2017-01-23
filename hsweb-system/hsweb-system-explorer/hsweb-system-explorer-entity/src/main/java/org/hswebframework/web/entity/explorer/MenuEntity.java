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

import org.hswebframework.web.commons.entity.*;
import org.hswebframework.web.entity.authorization.ActionEntity;

import java.util.List;
import java.util.Map;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface MenuEntity<C extends MenuEntity, A extends ActionEntity>
        extends TreeSortSupportEntity<String> {
    String getName();

    void setName(String name);

    String getDescribe();

    void setDescribe(String describe);

    String getPermissionId();

    void setPermissionId(String permissionId);

    String getUrl();

    void setUrl(String url);

    String getIcon();

    void setIcon(String icon);

    String getAuthentication();

    void setAuthentication(String authentication);

    Map<String, Object> getAuthenticationConfig();

    void setAuthenticationConfig(Map<String, Object> authenticationConfig);

    String getOnInit();

    void setOnInit(String onInit);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    List<A> getActions();

    void setActions(List<A> actions);

    void setChildren(List<C> children);

    @Override
    @SuppressWarnings("unchecked")
    List<C> getChildren();

    MenuEntity<C, A> clone();
}
