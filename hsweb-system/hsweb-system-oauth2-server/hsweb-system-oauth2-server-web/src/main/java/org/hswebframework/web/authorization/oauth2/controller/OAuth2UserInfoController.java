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

package org.hswebframework.web.authorization.oauth2.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationHolder;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.authorization.oauth2.server.OAuth2AccessToken;
import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;
import org.hswebframework.web.authorization.oauth2.server.token.AccessTokenService;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author zhouhao
 */
@RestController
@Api(tags = "OAuth2.0-服务-获取用户信息", value = "OAuth2.0-服务-获取用户信息")
@RequestMapping("${hsweb.web.mappings.oauth2-auth-info:oauth2/user-auth-info}")
public class OAuth2UserInfoController {


    @Resource
    private AccessTokenService accessTokenService;

    @GetMapping
    @ApiOperation("根据accessToken获取对应用户信息")
    public ResponseMessage<Authentication> getLoginUser(@RequestParam("access_token") String access_token) {
        OAuth2AccessToken auth2AccessEntity = accessTokenService.getTokenByAccessToken(access_token);
        if (null == auth2AccessEntity) {
            throw new GrantTokenException(ErrorType.EXPIRED_TOKEN);
        }
        return ResponseMessage.ok(AuthenticationHolder.get(auth2AccessEntity.getOwnerId()));
    }

    @GetMapping("/{userId}")
    @ApiOperation("根据accessToken获取特定的用户信息")
    public ResponseMessage<Authentication> getUserById(
            @PathVariable("userId") String userId,
            @RequestParam("access_token") String access_token) {
        OAuth2AccessToken auth2AccessEntity = accessTokenService.getTokenByAccessToken(access_token);
        if (null == auth2AccessEntity) {
            throw new GrantTokenException(ErrorType.EXPIRED_TOKEN);
        }
        if (auth2AccessEntity.getScope() == null ||(!auth2AccessEntity.getScope().contains("*")&&!auth2AccessEntity.getScope().contains("user:get"))) {
            throw new GrantTokenException(ErrorType.UNAUTHORIZED_CLIENT);
        }
        Authentication info=  AuthenticationHolder.get(userId);
        if(info==null){
            throw new NotFoundException("user:"+userId+" not found");
        }
        return ResponseMessage.ok(info);
    }

}
