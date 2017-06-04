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

import java.util.Collections;
import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimplePermissionRoleModel implements PermissionRoleModel {
//    private String roleId;

    private String permissionId;

    private List<String> actions;

    private List<DataAccessModel> dataAccesses;

    @Override
    public String getPermissionId() {
        return permissionId;
    }

    @Override
    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    @Override
    public List<String> getActions() {
        if (this.actions == null) return Collections.emptyList();
        return actions;
    }

    @Override
    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    @Override
    public List<DataAccessModel> getDataAccesses() {
        if (this.dataAccesses == null) return Collections.emptyList();
        return this.dataAccesses;
    }

    @Override
    public void setDataAccesses(List<DataAccessModel> dataAccesses) {
        this.dataAccesses = dataAccesses;
    }

}
