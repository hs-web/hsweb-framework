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

package org.hswebframework.web.authorization.oauth2.server.support.code;

import org.hswebframework.web.authorization.oauth2.server.OAuth2AccessToken;
import org.hswebframework.web.authorization.oauth2.server.client.OAuth2Client;
import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;
import org.hswebframework.web.authorization.oauth2.server.support.AbstractAuthorizationService;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.hswebframework.web.oauth2.core.GrantType;

import static org.hswebframework.web.oauth2.core.ErrorType.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DefaultAuthorizationCodeGranter extends AbstractAuthorizationService implements AuthorizationCodeGranter {

    //默认有效时间为10分钟
    private long codeTimeOut = 10 * 60 * 1000;

    private AuthorizationCodeService authorizationCodeService;

    public DefaultAuthorizationCodeGranter(AuthorizationCodeService authorizationCodeService) {
        this.authorizationCodeService = authorizationCodeService;
    }

    public void setCodeTimeOut(long codeTimeOut) {
        this.codeTimeOut = codeTimeOut;
    }

    @Override
    public OAuth2AccessToken requestToken(AuthorizationCodeTokenRequest request) {
        String clientId = request.getClientId();
        String clientSecret = request.getClientSecret();
        String code = request.getCode();
        String redirectUri = request.getRedirectUri();

        assertParameterNotBlank(clientId, ILLEGAL_CLIENT_ID);
        assertParameterNotBlank(clientSecret, ILLEGAL_CLIENT_SECRET);
        assertParameterNotBlank(code, ILLEGAL_CODE);
        assertParameterNotBlank(redirectUri, ILLEGAL_REDIRECT_URI);

        OAuth2Client client = getClient(clientId, clientSecret);
        assertGrantTypeSupport(client, GrantType.authorization_code);

        AuthorizationCode authorizationCode = authorizationCodeService.consumeAuthorizationCode(code);
        if (authorizationCode == null) {
            throw new GrantTokenException(ErrorType.ILLEGAL_CODE);
        }
        if (System.currentTimeMillis() - authorizationCode.getCreateTime() > codeTimeOut) {
            throw new GrantTokenException(ErrorType.EXPIRED_CODE);
        }
        // TODO: 17-5-3  验证redirect_uri
        //验证redirect_uri
        if (!redirectUri.equals(authorizationCode.getRedirectUri())) {
            //   throw new GrantTokenException(ILLEGAL_REDIRECT_URI);
        }

        OAuth2AccessToken accessToken = accessTokenService.createToken();
        accessToken.setGrantType(GrantType.authorization_code);
        accessToken.setScope(authorizationCode.getScope());
        accessToken.setOwnerId(authorizationCode.getUserId());
        accessToken.setExpiresIn(3600);
        accessToken.setClientId(clientId);
        return accessTokenService.saveOrUpdateToken(accessToken);
    }
}
