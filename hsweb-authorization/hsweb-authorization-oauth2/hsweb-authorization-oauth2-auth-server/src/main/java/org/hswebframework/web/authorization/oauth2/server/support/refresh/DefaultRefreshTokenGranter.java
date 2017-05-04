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

package org.hswebframework.web.authorization.oauth2.server.support.refresh;

import org.hswebframework.web.authorization.oauth2.server.OAuth2AccessToken;
import org.hswebframework.web.authorization.oauth2.server.client.OAuth2Client;
import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;
import org.hswebframework.web.authorization.oauth2.server.support.AbstractAuthorizationService;
import org.hswebframework.web.authorization.oauth2.server.support.code.AuthorizationCode;
import org.hswebframework.web.authorization.oauth2.server.support.code.AuthorizationCodeGranter;
import org.hswebframework.web.authorization.oauth2.server.support.code.AuthorizationCodeService;
import org.hswebframework.web.authorization.oauth2.server.support.code.AuthorizationCodeTokenRequest;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.hswebframework.web.oauth2.core.GrantType;

import java.util.Set;

import static org.hswebframework.web.oauth2.core.ErrorType.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DefaultRefreshTokenGranter extends AbstractAuthorizationService implements RefreshTokenGranter {

    //默认有效时间为1年
    private long refreshTokenTimeOut = 1 * 365 * 24 * 60 * 60 * 1000;

    public void setRefreshTokenTimeOut(long refreshTokenTimeOut) {
        this.refreshTokenTimeOut = refreshTokenTimeOut;
    }

    @Override
    public OAuth2AccessToken refreshToken(RefreshTokenRequest request) {
        String clientId = request.getClientId();
        String clientSecret = request.getClientSecret();
        String refreshToken = request.getRefreshToken();
        assertParameterNotBlank(clientId, ILLEGAL_CLIENT_ID);
        assertParameterNotBlank(clientSecret, ILLEGAL_CLIENT_SECRET);
        assertParameterNotBlank(refreshToken, ILLEGAL_REFRESH_TOKEN);

        OAuth2Client client = getClient(clientId, clientSecret);
        assertGrantTypeSupport(client, GrantType.refresh_token);

        OAuth2AccessToken accessToken = accessTokenService.getTokenByRefreshToken(refreshToken);
        if (accessToken == null) {
            throw new GrantTokenException(ILLEGAL_REFRESH_TOKEN);
        }
        if (System.currentTimeMillis() - accessToken.getCreateTime() > refreshTokenTimeOut) {
            throw new GrantTokenException(EXPIRED_REFRESH_TOKEN);
        }
        Set<String> newRange = request.getScope() != null ? request.getScope() : accessToken.getScope();
        if (!accessToken.getScope().containsAll(newRange)) {
            throw new GrantTokenException(ErrorType.SCOPE_OUT_OF_RANGE);
        }
        accessToken.setAccessToken(accessTokenService.createToken().getAccessToken());
        accessToken.setScope(newRange);
        accessToken.setUpdateTime(System.currentTimeMillis());
        return accessTokenService.saveOrUpdateToken(accessToken);
    }
}
