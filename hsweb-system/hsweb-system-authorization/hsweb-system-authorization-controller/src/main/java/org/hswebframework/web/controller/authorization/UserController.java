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

import org.hswebframework.web.authorization.Authorization;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.CreateController;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.authorization.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.hswebframework.web.controller.message.ResponseMessage.ok;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@RestController
@RequestMapping("${hsweb.web.mappings.user:user}")
@Authorize(permission = "user")
@AccessLogger("用户管理")
public class UserController implements QueryController<UserEntity, String, QueryParamEntity>, CreateController<UserEntity, String> {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseMessage list(QueryParamEntity param) {
        return QueryController.super.list(param)
                .exclude(UserEntity.class, "password", "salt");
    }

    @Override
    @SuppressWarnings("unchecked")
    public UserService getService() {
        return userService;
    }

    @Authorize(action = "update")
    @PutMapping(path = "/{id}")
    @AccessLogger("{update_by_primary_key}")
    public ResponseMessage updateByPrimaryKey(@PathVariable String id, @RequestBody UserEntity data) {
        data.setId(id);
        getService().update(data);
        return ok();
    }

    @Authorize(merge = false)
    @PutMapping(path = "/password")
    @AccessLogger("{update_password_login_user}")
    public ResponseMessage updateLoginUserPassword(Authorization authorization,
                                                   @RequestParam String password,
                                                   @RequestParam String oldPassword) {
        getService().updatePassword(authorization.getUser().getId(), oldPassword, password);
        return ok();
    }

    @Authorize(action = "update")
    @PutMapping(path = "/password/{id}")
    @AccessLogger("{update_password_by_id}")
    public ResponseMessage updateByPasswordPrimaryKey(@PathVariable String id,
                                                      @RequestParam String password,
                                                      @RequestParam String oldPassword) {
        getService().updatePassword(id, oldPassword, password);
        return ok();
    }

    @Authorize(action = "enable")
    @PutMapping(path = "/{id}/enable")
    @AccessLogger("{enable_user}")
    public ResponseMessage enable(@PathVariable String id) {
        return ok(getService().enable(id));
    }

    @Authorize(action = "disable")
    @PutMapping(path = "/{id}/disable")
    @AccessLogger("{disable_user}")
    public ResponseMessage disable(@PathVariable String id) {
        return ok(getService().disable(id));
    }
}
