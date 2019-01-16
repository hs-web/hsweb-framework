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
 *
 */

package org.hswebframework.web.authorization.controller.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hswebframework.web.commons.model.Model;

import java.util.List;

/**
 *
 * @author zhouhao
 */
@ApiModel(value = "PermissionRoleModel", description = "权限配置")
public interface PermissionRoleModel extends Model {
//
//    @ApiModelProperty(value = "角色ID", required = true, example = "admin")
//    String getRoleId();
//
//    void setRoleId(String roleId);

    @ApiModelProperty(value = "权限ID", required = true, example = "user")
    String getPermissionId();

    void setPermissionId(String permissionId);

    @ApiModelProperty(value = "可操作事件"
//            , example = "[\"query\",\"add\"]"
    )
    List<String> getActions();

    void setActions(List<String> actions);

    @ApiModelProperty(value = "数据级权限控制配置", dataType = "DataAccessModel")
    List<DataAccessModel> getDataAccesses();

    void setDataAccesses(List<DataAccessModel> dataAccesses);

}
