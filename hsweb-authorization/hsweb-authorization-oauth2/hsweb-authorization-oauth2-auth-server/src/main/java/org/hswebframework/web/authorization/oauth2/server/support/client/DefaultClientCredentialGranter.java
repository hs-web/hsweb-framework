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

package org.hswebframework.web.authorization.oauth2.server.support.client;

import org.hswebframework.web.authorization.oauth2.server.OAuth2AccessToken;
import org.hswebframework.web.authorization.oauth2.server.client.OAuth2Client;
import org.hswebframework.web.authorization.oauth2.server.support.AbstractAuthorizationService;
import org.hswebframework.web.oauth2.core.GrantType;

import static org.hswebframework.web.oauth2.core.ErrorType.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DefaultClientCredentialGranter extends AbstractAuthorizationService implements ClientCredentialGranter {

    @Override
    public OAuth2AccessToken requestToken(ClientCredentialRequest request) {
        String clientId = request.getClientId();
        String clientSecret = request.getClientSecret();

        assertParameterNotBlank(clientId, ILLEGAL_CLIENT_ID);
        assertParameterNotBlank(clientSecret, ILLEGAL_CLIENT_SECRET);

        OAuth2Client client = getClient(clientId, clientSecret);
        assertGrantTypeSupport(client, GrantType.client_credentials);

        OAuth2AccessToken accessToken = accessTokenService.createToken();
        // 设置自定义的属性,其他属性在create的时候已经被设置
        accessToken.setOwnerId(client.getOwnerId());
        accessToken.setExpiresIn(3600);
        accessToken.setScope(client.getDefaultGrantScope());
        accessToken.setClientId(client.getId());
        accessToken.setGrantType(GrantType.client_credentials);

        //保存token
        return accessTokenService.saveOrUpdateToken(accessToken);
    }
}
