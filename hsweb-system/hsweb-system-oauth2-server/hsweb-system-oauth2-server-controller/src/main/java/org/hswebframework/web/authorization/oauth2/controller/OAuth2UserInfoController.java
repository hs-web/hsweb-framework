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

package org.hswebframework.web.authorization.oauth2.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.AuthorizeException;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationHolder;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.oauth2.api.OAuth2ServerService;
import org.hswebframework.web.authorization.oauth2.api.entity.OAuth2AccessEntity;
import org.hswebframework.web.oauth2.model.AuthorizationCodeModel;
import org.hswebframework.web.oauth2.model.ImplicitAccessTokenModel;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@RestController
@Api(tags = "hsweb-oauth2", description = "OAuth2授权", hidden = true)
@RequestMapping("${hsweb.web.mappings.oauth2-auth-info:oauth2/user-auth-info}")
public class OAuth2UserInfoController {

    @Resource
    private OAuth2ServerService oAuth2ServerService;

    @GetMapping
    @ApiOperation("根据accessToken获取用户信息")
    public Authentication getLoginUser(@RequestParam("access_token") String access_token) {
        OAuth2AccessEntity auth2AccessEntity = oAuth2ServerService.getAccessToken(access_token);
        if (null == auth2AccessEntity) {
            throw new AuthorizeException();
        }
        return AuthenticationHolder.get(auth2AccessEntity.getUserId());
    }


    @GetMapping("/{userId}")
    @ApiOperation("根据accessToken获取用户信息")
    public Authentication getUserById(
            @PathVariable("userId") String userId,
            @RequestParam("access_token") String access_token) {
        OAuth2AccessEntity auth2AccessEntity = oAuth2ServerService.getAccessToken(access_token);
        if (null == auth2AccessEntity) {
            throw new AuthorizeException();
        }
        return AuthenticationHolder.get(userId);
    }

}
