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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hswebframework.web.commons.model.Model;

import java.util.List;

/**
 * @author zhouhao
 */
@ApiModel(value = "RoleModel", description = "角色模型")
public interface RoleModel extends Model {

    @ApiModelProperty(value = "角色ID", readOnly = true, required = true, example = "admin")
    String getId();

    void setId(String id);

    @ApiModelProperty(value = "角色名", required = true)
    String getName();

    void setName(String name);

    @ApiModelProperty("说明")
    String getDescribe();

    void setDescribe(String describe);

    @ApiModelProperty(value = "权限配置", dataType = "PermissionRoleModel")
    List<PermissionRoleModel> getPermissions();

    void setPermissions(List<PermissionRoleModel> permissions);
}
