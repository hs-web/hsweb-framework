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
import org.hswebframework.web.authorization.oauth2.server.OAuth2AccessToken;
import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;
import org.hswebframework.web.authorization.oauth2.server.support.OAuth2Granter;
import org.hswebframework.web.authorization.oauth2.server.support.client.HttpClientCredentialRequest;
import org.hswebframework.web.authorization.oauth2.server.support.code.HttpAuthorizationCodeTokenRequest;
import org.hswebframework.web.authorization.oauth2.server.support.implicit.HttpImplicitRequest;
import org.hswebframework.web.authorization.oauth2.server.support.password.HttpPasswordRequest;
import org.hswebframework.web.authorization.oauth2.server.support.refresh.HttpRefreshTokenRequest;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.hswebframework.web.oauth2.core.GrantType;
import org.hswebframework.web.oauth2.model.AccessTokenModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author zhouhao
 */
@RestController
@Api(tags = "hsweb-oauth2", description = "OAuth2授权token获取", hidden = true)
@RequestMapping("${hsweb.web.mappings.authorize-oauth2:oauth2/token}")
public class OAuth2TokenController {

    @Resource
    private OAuth2Granter oAuth2Granter;

    @PostMapping
    public AccessTokenModel requestToken(
            @RequestParam("grant_type") String grant_type,
            HttpServletRequest request) {
        OAuth2AccessToken accessToken = null;
        switch (grant_type) {
            case GrantType.authorization_code:
                accessToken = oAuth2Granter.grant(GrantType.authorization_code, new HttpAuthorizationCodeTokenRequest(request));
                break;
            case GrantType.client_credentials:
                accessToken = oAuth2Granter.grant(GrantType.client_credentials, new HttpClientCredentialRequest(request));
                break;
            case GrantType.implicit:
                accessToken = oAuth2Granter.grant(GrantType.implicit, new HttpImplicitRequest(request));
                break;
            case GrantType.password:
                accessToken = oAuth2Granter.grant(GrantType.password, new HttpPasswordRequest(request));
                break;
            case GrantType.refresh_token:
                accessToken = oAuth2Granter.grant(GrantType.refresh_token, new HttpRefreshTokenRequest(request));
                break;
            default:
                ErrorType.UNSUPPORTED_GRANT_TYPE.throwThis(GrantTokenException::new);
        }
        return entityToModel(accessToken);
    }


    protected AccessTokenModel entityToModel(OAuth2AccessToken token) {
        AccessTokenModel model = new AccessTokenModel();
        model.setAccess_token(token.getAccessToken());
        model.setRefresh_token(token.getRefreshToken());
        model.setExpires_in(token.getExpiresIn());
        if (token.getScope() != null)
            model.setScope(token.getScope().stream().reduce((t1, t2) -> t1.concat(",").concat(t2)).orElse(""));
        else
            model.setScope("public");
        model.setToken_type("bearer");
        return model;
    }

}
