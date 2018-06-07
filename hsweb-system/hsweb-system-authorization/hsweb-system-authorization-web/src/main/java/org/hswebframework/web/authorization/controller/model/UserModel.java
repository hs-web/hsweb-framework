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

package org.hswebframework.web.authorization.controller.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hswebframework.web.commons.model.Model;

/**
 * @author zhouhao
 */
@ApiModel(value = "UserModel", description = "用户信息")
public interface UserModel extends Model {
    void setName(String name);

    @ApiModelProperty(value = "用户名", required = true, readOnly = true)
    String getUsername();

    void setUsername(String username);

    @ApiModelProperty(value = "用户姓名", required = true)
    String getName();

    void setPassword(String password);

    @ApiModelProperty(value = "密码", required = true)
    String getPassword();
}
