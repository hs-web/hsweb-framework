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

package org.hswebframework.web.model.authorization;


import java.util.ArrayList;
import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleRoleModel implements RoleModel {
    private String id;

    private String name;

    private String describe;

    private List<SimplePermissionRoleModel> permissions;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
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

    @Override
    public List<PermissionRoleModel> getPermissions() {
        if (null == permissions) permissions = new ArrayList<>();
        return new ArrayList<>(permissions);
    }

    public void setPermissions(List<PermissionRoleModel> permissions) {
        this.permissions = ((List) permissions);
    }
}
