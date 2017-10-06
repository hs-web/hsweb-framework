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
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.authorization.oauth2.server.OAuth2AccessToken;
import org.hswebframework.web.authorization.oauth2.server.support.OAuth2Granter;
import org.hswebframework.web.authorization.oauth2.server.support.code.AuthorizationCodeRequest;
import org.hswebframework.web.authorization.oauth2.server.support.code.AuthorizationCodeService;
import org.hswebframework.web.authorization.oauth2.server.support.code.HttpAuthorizationCodeRequest;
import org.hswebframework.web.authorization.oauth2.server.support.implicit.HttpImplicitRequest;
import org.hswebframework.web.authorization.oauth2.server.support.implicit.ImplicitRequest;
import org.hswebframework.web.oauth2.core.GrantType;
import org.hswebframework.web.oauth2.model.AuthorizationCodeModel;
import org.hswebframework.web.oauth2.model.ImplicitAccessTokenModel;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@RestController
@Api(tags = "hsweb-oauth2", description = "OAuth2授权", hidden = true)
@RequestMapping("${hsweb.web.mappings.authorize-oauth2:oauth2/authorize}")
public class OAuth2AuthorizeController {

    @Resource
    private AuthorizationCodeService authorizationCodeService;

    @Resource
    private OAuth2Granter oAuth2Granter;


    @GetMapping(params = "response_type=code")
    @ApiOperation("登录用户获取OAuth2.0授权码")
    @Authorize
    public AuthorizationCodeModel requestCode(
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(value = "state", required = false) String state,
            HttpServletRequest request) {
        Authentication authentication = Authentication.current().orElseThrow(UnAuthorizedException::new);

        AuthorizationCodeRequest codeRequest = new HttpAuthorizationCodeRequest(authentication.getUser().getId(), request);

        String code = authorizationCodeService.createAuthorizationCode(codeRequest);

        AuthorizationCodeModel model = new AuthorizationCodeModel();
        model.setCode(code);
        model.setRedirectUri(redirectUri);
        model.setState(state);
        return model;
    }


    @GetMapping(params = "response_type=token")
    @ApiOperation("implicit方式授权")
    public ImplicitAccessTokenModel authorizeByImplicit(
            @RequestParam(value = "redirect_uri") String redirect_uri,
            @RequestParam(value = "state") String state,
            HttpServletRequest request) {

        ImplicitRequest implicitRequest = new HttpImplicitRequest(request);
        OAuth2AccessToken accessToken = oAuth2Granter.grant(GrantType.implicit, implicitRequest);

        ImplicitAccessTokenModel model = new ImplicitAccessTokenModel();
        model.setState(state);
        model.setToken_type("example");
        model.setAccess_token(accessToken.getAccessToken());
        model.setExpires_in(accessToken.getExpiresIn());
        model.setRedirect_uri(redirect_uri);
        return model;
    }

}
