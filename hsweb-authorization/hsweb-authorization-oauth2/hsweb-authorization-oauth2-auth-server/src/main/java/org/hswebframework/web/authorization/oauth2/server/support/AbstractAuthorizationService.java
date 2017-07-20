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

package org.hswebframework.web.authorization.oauth2.server.support;

import org.hswebframework.web.authorization.oauth2.server.client.OAuth2Client;
import org.hswebframework.web.authorization.oauth2.server.client.OAuth2ClientService;
import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;
import org.hswebframework.web.authorization.oauth2.server.token.AccessTokenService;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.oauth2.core.ErrorType;

import static org.hswebframework.web.oauth2.core.ErrorType.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public abstract class AbstractAuthorizationService {
    protected AccessTokenService  accessTokenService;
    protected OAuth2ClientService clientService;

    public AccessTokenService getAccessTokenService() {
        return accessTokenService;
    }

    public void setAccessTokenService(AccessTokenService accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    public OAuth2ClientService getClientService() {
        return clientService;
    }

    public void setClientService(OAuth2ClientService clientService) {
        this.clientService = clientService;
    }

    protected void assertGrantTypeSupport(OAuth2Client client, String grantType) {
        if (!client.isSupportGrantType(grantType)) {
            throw new GrantTokenException(UNSUPPORTED_GRANT_TYPE);
        }
    }

    protected void assertParameterNotBlank(String parameter, ErrorType type) {
        if (null == parameter || parameter.isEmpty()) {
            throw new GrantTokenException(type);
        }
    }

    protected OAuth2Client getClient(String clientId, String clientSecret) {
        OAuth2Client client = getClient(clientId);
        if (!client.getSecret().equals(clientSecret)) {
            throw new GrantTokenException(ILLEGAL_CLIENT_SECRET);
        }
        return client;
    }

    protected OAuth2Client checkClient(OAuth2Client client) {
        if (client == null) {
            throw new GrantTokenException(CLIENT_NOT_EXIST);
        }
        if (DataStatus.STATUS_ENABLED != client.getStatus()) {
            throw new GrantTokenException(CLIENT_DISABLED);
        }
        return client;
    }

    protected OAuth2Client getClientByOwnerId(String ownerId) {
        return checkClient(clientService.getClientByOwnerId(ownerId));
    }

    protected OAuth2Client getClient(String clientId) {
        return checkClient(clientService.getClientById(clientId));
    }

}
