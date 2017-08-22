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

package org.hswebframework.web.controller.authorization;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.authorization.token.TokenState;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.CreateController;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.entity.authorization.bind.BindRoleUserEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.authorization.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

import static org.hswebframework.web.controller.message.ResponseMessage.ok;

/**
 * 用户管理控制器
 *
 * @author zhouhao
 */
@RestController
@RequestMapping("${hsweb.web.mappings.user:user}")
@Authorize(permission = "user")
@AccessLogger("用户管理")
@Api(tags = "user-manager", description = "用户基本信息管理")
public class UserController implements
        QueryController<UserEntity, String, QueryParamEntity>,
        CreateController<BindRoleUserEntity, String, BindRoleUserEntity> {

    private UserService userService;

    private UserTokenManager userTokenManager;

    @Override
    @SuppressWarnings("unchecked")
    public UserService getService() {
        return userService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired(required = false)
    public void setUserTokenManager(UserTokenManager userTokenManager) {
        this.userTokenManager = userTokenManager;
    }

    @GetMapping("/tokens")
    @Authorize(action = Permission.ACTION_QUERY)
    @AccessLogger("获取所有已登录用户的信息")
    public ResponseMessage<List<UserToken>> userTokens() {
        if (userTokenManager == null) throw new UnsupportedOperationException("userTokenManager is null");

        return ok(userTokenManager.allLoggedUser());
    }

    @PutMapping("/tokens/{token}/{state}")
    @Authorize(action = "change-state")
    @AccessLogger("修改token的状态")
    public ResponseMessage<List<UserToken>> makeOffline(@PathVariable String token, @PathVariable TokenState state) {
        if (userTokenManager == null) throw new UnsupportedOperationException("userTokenManager is null");
        userTokenManager.changeTokenState(token, state);
        return ok();
    }

    @Override
    public ResponseMessage<PagerResult<UserEntity>> list(QueryParamEntity param) {
        param.excludes("password", "salt");
        return QueryController.super.list(param)
                .exclude(UserEntity.class, "password", "salt");
    }

    @Override
    public ResponseMessage<UserEntity> getByPrimaryKey(@PathVariable String id) {
        return QueryController.super.getByPrimaryKey(id)
                .exclude(UserEntity.class, "password", "salt");
    }

    @Authorize(action = "update")
    @PutMapping(path = "/{id:.+}")
    @AccessLogger("{update_by_primary_key}")
    @ApiOperation("根据ID修改用户信息")
    public ResponseMessage<Void> updateByPrimaryKey(@PathVariable String id,
                                                    @RequestBody BindRoleUserEntity userModel) {
        getService().update(id, userModel);
        return ok();
    }

    @Authorize(merge = false)
    @PutMapping(path = "/password")
    @AccessLogger("{update_password_login_user}")
    @ApiOperation("修改当前用户的密码")
    public ResponseMessage<Void> updateLoginUserPassword(@RequestParam String password,
                                                         @RequestParam String oldPassword) {

        Authentication authentication = Authentication.current().orElseThrow(UnAuthorizedException::new);
        getService().updatePassword(authentication.getUser().getId(), oldPassword, password);
        return ok();
    }

    @Authorize(action = Permission.ACTION_UPDATE)
    @PutMapping(path = "/password/{id:.+}")
    @AccessLogger("{update_password_by_id}")
    @ApiOperation("修改指定用户的密码")
    public ResponseMessage<Void> updateByPasswordPrimaryKey(@PathVariable String id,
                                                            @RequestParam String password,
                                                            @RequestParam String oldPassword) {
        getService().updatePassword(id, oldPassword, password);
        return ok();
    }

    @Override
    public ResponseMessage<String> add(@RequestBody BindRoleUserEntity data) {
        Authentication authentication = Authentication.current().orElse(null);
        if (null != authentication) {
            data.setCreatorId(authentication.getUser().getId());
        }
        return CreateController.super.add(data);
    }

    @Authorize(action = "enable")
    @PutMapping(path = "/{id}/enable")
    @AccessLogger("{enable_user}")
    @ApiOperation("启用用户")
    public ResponseMessage<Boolean> enable(@PathVariable String id) {
        return ok(getService().enable(id));
    }

    @Authorize(action = "disable")
    @PutMapping(path = "/{id}/disable")
    @AccessLogger("{disable_user}")
    @ApiOperation("禁用用户")
    public ResponseMessage<Boolean> disable(@PathVariable String id) {
        return ok(getService().disable(id));
    }

    @Override
    public BindRoleUserEntity modelToEntity(BindRoleUserEntity model, BindRoleUserEntity entity) {
        return model;
    }
}
