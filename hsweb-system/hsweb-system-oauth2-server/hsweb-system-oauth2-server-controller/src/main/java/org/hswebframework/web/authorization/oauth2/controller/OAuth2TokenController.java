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
import org.apache.commons.codec.binary.Base64;
import org.hswebframework.web.authorization.oauth2.api.OAuth2ServerService;
import org.hswebframework.web.authorization.oauth2.api.entity.OAuth2AccessEntity;
import org.hswebframework.web.oauth2.model.AccessTokenModel;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * @author zhouhao
 */
@RestController
@Api(tags = "hsweb-oauth2", description = "OAuth2授权token获取", hidden = true)
@RequestMapping("${hsweb.web.mappings.authorize-oauth2:oauth2/token}")
public class OAuth2TokenController {

    @Resource
    private OAuth2ServerService oAuth2ServerService;

    @PostMapping(params = "grant_type=authorization_code")
    @ApiOperation("authorization_code方式授权")
    public AccessTokenModel authorizeByCode(
            @RequestParam("code") String code,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            @RequestParam(value = "redirect_uri") String redirect_uri,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "scope", required = false) String scope) {

        String[] clientCredentials = getClientCredentials(clientId, clientSecret, authorization);
        clientId = clientCredentials[0];
        clientSecret = clientCredentials[1];
        AccessTokenModel model = entityToModel(oAuth2ServerService.requestTokenByCode(code, clientId, clientSecret, scope, redirect_uri));
        return model;
    }

    @PostMapping(params = "grant_type=client_credentials")
    @ApiOperation("client_credentials方式授权")
    public AccessTokenModel authorizeByClientCredentials(
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String[] clientCredentials = getClientCredentials(clientId, clientSecret, authorization);
        clientId = clientCredentials[0];
        clientSecret = clientCredentials[1];
        AccessTokenModel model = entityToModel(oAuth2ServerService.requestTokenByClientCredential(clientId, clientSecret));
        return model;
    }

    @PostMapping(params = "grant_type=password")
    @ApiOperation("password方式授权")
    public AccessTokenModel authorizeByPassword(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "password") String password,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String[] clientCredentials = getClientCredentials(username, password, authorization);
        username = clientCredentials[0];
        password = clientCredentials[1];
        AccessTokenModel model = entityToModel(oAuth2ServerService.requestTokenByPassword(username, password));
        return model;
    }

    @PostMapping(params = "grant_type=refresh_token")
    @ApiOperation("刷新授权码")
    public AccessTokenModel refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            @RequestParam(value = "refresh_token") String refreshToken,
            @RequestParam(value = "scope", required = false) String scope) {

        String[] clientCredentials = getClientCredentials(clientId, clientSecret, authorization);
        clientId = clientCredentials[0];
        clientSecret = clientCredentials[1];

        AccessTokenModel model = entityToModel(oAuth2ServerService.refreshToken(clientId, clientSecret, refreshToken, scope));
        return model;
    }

    protected String[] getClientCredentials(String clientId, String clientSecret, String authorization) {
        if ((clientId == null || clientSecret == null) && authorization == null) {
            throw new IllegalArgumentException("authorization error!");
        }
        if (!isEmpty(authorization)) {
            String[] creds = decodeClientAuthenticationHeader(authorization);
            Assert.notNull(creds, "");
            if (creds.length > 1) {
                clientId = creds[0];
                clientSecret = creds[1];
            } else {
                clientSecret = creds[0];
            }
        }
        Assert.hasLength(clientId, "");
        Assert.hasLength(clientSecret, "");
        return new String[]{clientId, clientSecret};
    }

    protected AccessTokenModel entityToModel(OAuth2AccessEntity entity) {
        AccessTokenModel model = new AccessTokenModel();
        model.setAccess_token(entity.getAccessToken());
        model.setRefresh_token(entity.getRefreshToken());
        model.setExpires_in(entity.getExpiresIn());
        model.setScope(entity.getScope());
        model.setToken_type("bearer");
        return model;
    }


    protected static String[] decodeClientAuthenticationHeader(String authenticationHeader) {
        if (isEmpty(authenticationHeader)) {
            return null;
        } else {
            String[] tokens = authenticationHeader.split(" ");
            if (tokens.length != 2) {
                return null;
            } else {
                String authType = tokens[0];
                if (!"basic".equalsIgnoreCase(authType)) {
                    return null;
                } else {
                    String encodedCreds = tokens[1];
                    return decodeBase64EncodedCredentials(encodedCreds);
                }
            }
        }
    }

    protected static String[] decodeBase64EncodedCredentials(String encodedCreds) {
        String decodedCreds = new String(Base64.decodeBase64(encodedCreds));
        String[] creds = decodedCreds.split(":", 2);
        return creds.length != 2 ? null : (!isEmpty(creds[0]) && !isEmpty(creds[1]) ? creds : null);
    }
}
