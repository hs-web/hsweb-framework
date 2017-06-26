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

package org.hswebframework.web.controller.authorization;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.authorization.RoleEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.authorization.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hswebframework.web.controller.message.ResponseMessage.ok;

/**
 * 角色控制器
 *
 * @author zhouhao
 */
@RestController
@RequestMapping("${hsweb.web.mappings.role:role}")
@AccessLogger("{role_manager}")
@Authorize(permission = "role")
@Api(tags = "role-manager", description = "角色管理")
public class RoleController implements SimpleGenericEntityController<RoleEntity, String, QueryParamEntity> {

    @Autowired
    private RoleService roleService;

    @Override
    public RoleService getService() {
        return roleService;
    }

    @PutMapping("/disable/{id:.+}")
    @Authorize(action = Permission.ACTION_DISABLE)
    @AccessLogger("{disable}")
    @ApiOperation("禁用角色")
    public ResponseMessage disable(@PathVariable String id) {
        roleService.disable(id);
        return ok();
    }

    @PutMapping("/enable/{id}")
    @Authorize(action = Permission.ACTION_ENABLE)
    @AccessLogger("{disable}")
    @ApiOperation("启用角色")
    public ResponseMessage enable(@PathVariable String id) {
        roleService.enable(id);
        return ok();
    }
}
