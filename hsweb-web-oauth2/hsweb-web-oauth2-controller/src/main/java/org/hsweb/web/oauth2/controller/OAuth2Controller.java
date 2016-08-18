/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.oauth2.controller;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.core.exception.AuthorizeException;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.oauth2.po.OAuth2Access;
import org.hsweb.web.oauth2.po.OAuth2Client;
import org.hsweb.web.oauth2.service.OAuth2ClientService;
import org.hsweb.web.oauth2.service.OAuth2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author zhouhao
 */
@RestController
@RequestMapping("/oauth2")
@AccessLogger("oauth2授权")
public class OAuth2Controller {

    @Autowired(required = false)
    private OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

    @Resource
    private OAuth2ClientService oAuth2ClientService;

    @Resource
    private OAuth2Service oAuth2Service;

    @RequestMapping(value = "/access_token", method = {RequestMethod.POST})
    @AccessLogger("获取access_token")
    public ResponseMessage accessToken(@RequestParam(value = OAuth.OAUTH_GRANT_TYPE, defaultValue = "client_credentials") String grantType,
                                       HttpServletRequest request) {
        try {
            OAuthTokenRequest tokenRequest = new OAuthTokenRequest(request);
            //获取OAuth客户端
            String clientId = tokenRequest.getClientId();
            String clientSecret = tokenRequest.getClientSecret();
            OAuth2Client client = oAuth2ClientService.selectSingle(QueryParam.build()
                    .where("id", clientId)
                    .and("secret", clientSecret).and("status", 1));
            //验证客户端
            if (null == client) {
                throw new AuthorizeException(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT);
            }
            //目前只支持client_credentials方式
            if (grantType.equals(GrantType.CLIENT_CREDENTIALS.toString())) {
                String userId = client.getUserId();
                String accessToken = oauthIssuerImpl.accessToken();
                String refreshToken = oauthIssuerImpl.refreshToken();
                OAuth2Access access = new OAuth2Access();
                access.setExpireIn(oAuth2Service.getDefaultExpireIn());
                access.setCreateDate(new Date());
                access.setAccessToken(accessToken);
                access.setRefreshToken(refreshToken);
                access.setUserId(userId);
                access.setClientId(clientId);
                oAuth2Service.addAccessToken(access);
                OAuthResponse response = OAuthASResponse
                        .tokenResponse(HttpServletResponse.SC_OK)
                        .setTokenType("bearer")
                        .setAccessToken(accessToken)
                        .setExpiresIn(String.valueOf(oAuth2Service.getDefaultExpireIn()))
                        .setRefreshToken(refreshToken)
                        .setScope("public")
                        .buildJSONMessage();
                return ResponseMessage.ok(response.getBody()).onlyData();
            } else {
                throw new AuthorizeException(OAuthError.TokenResponse.UNSUPPORTED_GRANT_TYPE);
            }
        } catch (Exception e) {
            throw new AuthorizeException(e.getMessage(), e, 401);
        }
    }
}
