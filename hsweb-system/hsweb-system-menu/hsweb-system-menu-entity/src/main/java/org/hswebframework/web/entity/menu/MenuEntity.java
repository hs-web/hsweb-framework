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
 *
 */

package org.hswebframework.web.entity.menu;

import org.hswebframework.web.commons.entity.*;
import org.hswebframework.web.entity.authorization.ActionEntity;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface MenuEntity<C extends MenuEntity>
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

    boolean isEnabled();

    void setEnabled(boolean enabled);

    void setChildren(List<C> children);

    @Override
    @SuppressWarnings("unchecked")
    List<C> getChildren();

    MenuEntity<C> clone();
}
