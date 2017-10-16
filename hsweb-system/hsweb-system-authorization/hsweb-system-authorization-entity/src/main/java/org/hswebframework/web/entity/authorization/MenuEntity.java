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

package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.TreeSortSupportEntity;

import java.util.List;

/**
 * @author zhouhao
 */
public interface MenuEntity
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

    Byte getStatus();

    void setStatus(Byte status);

    void setChildren(List<MenuEntity> children);

    @Override
    MenuEntity clone();
}
