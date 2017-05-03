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

package org.hswebframework.web.authorization.oauth2.server.support.password;

import org.hswebframework.web.authorization.oauth2.server.OAuth2AccessToken;
import org.hswebframework.web.authorization.oauth2.server.client.OAuth2Client;
import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;
import org.hswebframework.web.authorization.oauth2.server.support.AbstractAuthorizationService;
import org.hswebframework.web.authorization.oauth2.server.support.implicit.ImplicitGranter;
import org.hswebframework.web.authorization.oauth2.server.support.implicit.ImplicitRequest;
import org.hswebframework.web.oauth2.core.GrantType;

import java.util.Set;

import static org.hswebframework.web.oauth2.core.ErrorType.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DefaultPasswordGranter extends AbstractAuthorizationService implements PasswordGranter {
    private PasswordService passwordService;

    public DefaultPasswordGranter(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @Override
    public OAuth2AccessToken requestToken(PasswordRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        Set<String> scope = request.getScope();

        assertParameterNotBlank(username, ILLEGAL_USERNAME);
        assertParameterNotBlank(password, ILLEGAL_PASSWORD);

        String userId = passwordService.getUserIdByUsernameAndPassword(username, password);

        assertParameterNotBlank(userId, USER_NOT_EXIST);

        OAuth2Client client = getClientByOwnerId(userId);
        assertGrantTypeSupport(client, GrantType.implicit);
        if (scope == null || scope.isEmpty())
            scope = client.getDefaultGrantScope();
        if (!client.getDefaultGrantScope().containsAll(scope)) {
            throw new GrantTokenException(SCOPE_OUT_OF_RANGE);
        }

        OAuth2AccessToken accessToken = accessTokenService.createToken();
        accessToken.setGrantType(GrantType.password);
        accessToken.setScope(scope);
        accessToken.setOwnerId(userId);
        accessToken.setExpiresIn(3600);
        accessToken.setClientId(client.getId());
        return accessTokenService.saveOrUpdateToken(accessToken);
    }
}
