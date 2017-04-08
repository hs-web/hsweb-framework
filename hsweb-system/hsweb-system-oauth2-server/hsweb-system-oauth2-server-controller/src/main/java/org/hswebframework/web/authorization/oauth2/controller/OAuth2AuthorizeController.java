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
@RequestMapping("${hsweb.web.mappings.authorize-oauth2:oauth2/authorize}")
public class OAuth2AuthorizeController {

    @Resource
    private OAuth2ServerService oAuth2ServerService;


    @GetMapping(params = "response_type=code")
    @ApiOperation("登录用户获取OAuth2.0授权码")
    @Authorize
    public AuthorizationCodeModel requestCode(
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state) {
        Authentication authentication = Authentication.current().orElseThrow(AuthorizeException::new);
        String code = oAuth2ServerService.requestCode(clientId, authentication.getUser().getId(), scope,redirectUri);
        AuthorizationCodeModel model = new AuthorizationCodeModel();
        model.setCode(code);
        model.setRedirectUri(redirectUri);
        model.setState(state);
        return model;
    }


    @GetMapping(params = "response_type=token")
    @ApiOperation("implicit方式授权")
    public ImplicitAccessTokenModel authorizeByImplicit(
            @RequestParam(value = "client_id") String client_id,
            @RequestParam(value = "redirect_uri") String redirect_uri,
            @RequestParam(value = "state") String state,
            @RequestParam(value = "scope", required = false) String scope) {

        // TODO: 17-4-7  用户是否为当前登录的用户,而非client绑定的用户?
        // TODO: 17-3-6  validate redirect_uri
        OAuth2AccessEntity accessEntity = oAuth2ServerService.requestTokenByImplicit(client_id, scope);
        ImplicitAccessTokenModel model = new ImplicitAccessTokenModel();
        model.setState(state);
        model.setToken_type("example");
        model.setAccess_token(accessEntity.getAccessToken());
        model.setExpires_in(accessEntity.getExpiresIn());
        model.setRedirect_uri(redirect_uri);
        return model;
    }

}
