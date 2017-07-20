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

package org.hswebframework.web.authorization.oauth2.server.support.implicit;

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
public class DefaultImplicitGranter extends AbstractAuthorizationService implements ImplicitGranter {

    @Override
    public OAuth2AccessToken requestToken(ImplicitRequest request) {
        String clientId = request.getClientId();
        Set<String> scope = request.getScope();

        assertParameterNotBlank(clientId, ILLEGAL_CLIENT_ID);

        OAuth2Client client = getClient(clientId);
        assertGrantTypeSupport(client, GrantType.implicit);
        if (scope == null || scope.isEmpty())
            scope = client.getDefaultGrantScope();
        if (!client.getDefaultGrantScope().containsAll(scope)) {
            throw new GrantTokenException(SCOPE_OUT_OF_RANGE);
        }
        if (!client.getRedirectUri().equals(request.getRedirectUri())) {
            throw new GrantTokenException(ILLEGAL_REDIRECT_URI);
        }

        OAuth2AccessToken accessToken = accessTokenService.createToken();
        accessToken.setGrantType(GrantType.implicit);
        accessToken.setScope(scope);
        accessToken.setOwnerId(client.getOwnerId());
        accessToken.setExpiresIn(3600);
        accessToken.setClientId(clientId);
        return accessTokenService.saveOrUpdateToken(accessToken);
    }
}
