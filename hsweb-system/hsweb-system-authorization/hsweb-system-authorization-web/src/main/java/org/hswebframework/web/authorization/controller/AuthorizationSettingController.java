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
 */

package org.hswebframework.web.authorization.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.authorization.AuthorizationSettingEntity;
import org.hswebframework.web.service.authorization.AuthorizationSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限设置
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.autz-setting:autz-setting}")
@Authorize(permission = "autz-setting",description = "权限设置")
@Api(tags = "权限-权限设置",value = "权限设置")
public class AuthorizationSettingController implements SimpleGenericEntityController<AuthorizationSettingEntity, String, QueryParamEntity> {

    private AuthorizationSettingService authorizationSettingService;

    @Autowired
    public void setAuthorizationSettingService(AuthorizationSettingService authorizationSettingService) {
        this.authorizationSettingService = authorizationSettingService;
    }

    @Override
    public AuthorizationSettingService getService() {
        return authorizationSettingService;
    }

    @GetMapping("/{type}/{settingFor}")
    @Authorize(action = Permission.ACTION_GET)
    @ApiOperation("根据type和settingFor获取配置")
    public ResponseMessage<AuthorizationSettingEntity> select(@PathVariable String type, @PathVariable String settingFor) {
        return ResponseMessage.ok(authorizationSettingService.select(type, settingFor));
    }
}
