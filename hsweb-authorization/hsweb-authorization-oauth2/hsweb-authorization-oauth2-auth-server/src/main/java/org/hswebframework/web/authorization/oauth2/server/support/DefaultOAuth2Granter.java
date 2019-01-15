/*
 *  Copyright 2019 http://www.hswebframework.org
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

import org.hswebframework.web.authorization.oauth2.server.TokenRequest;
import org.hswebframework.web.authorization.oauth2.server.OAuth2AccessToken;
import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;
import org.hswebframework.web.authorization.oauth2.server.support.code.AuthorizationCodeTokenRequest;
import org.hswebframework.web.authorization.oauth2.server.support.code.AuthorizationCodeGranter;
import org.hswebframework.web.authorization.oauth2.server.support.client.ClientCredentialRequest;
import org.hswebframework.web.authorization.oauth2.server.support.client.ClientCredentialGranter;
import org.hswebframework.web.authorization.oauth2.server.support.implicit.ImplicitRequest;
import org.hswebframework.web.authorization.oauth2.server.support.implicit.ImplicitGranter;
import org.hswebframework.web.authorization.oauth2.server.support.password.PasswordRequest;
import org.hswebframework.web.authorization.oauth2.server.support.password.PasswordGranter;
import org.hswebframework.web.authorization.oauth2.server.support.refresh.RefreshTokenRequest;
import org.hswebframework.web.authorization.oauth2.server.support.refresh.RefreshTokenGranter;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.hswebframework.web.oauth2.core.GrantType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.hswebframework.web.oauth2.core.ErrorType.ILLEGAL_GRANT_TYPE;
import static org.hswebframework.web.oauth2.core.ErrorType.UNSUPPORTED_GRANT_TYPE;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DefaultOAuth2Granter implements OAuth2Granter {

    private Map<String, Granter> supportGranter = new HashMap<>(5);

    public DefaultOAuth2Granter addAuthorizationCodeSupport(AuthorizationCodeGranter authorizationCodeService) {
        return addGranter(GrantType.authorization_code, AuthorizationCodeTokenRequest.class, authorizationCodeService::requestToken);
    }

    public DefaultOAuth2Granter addRefreshTokenSupport(RefreshTokenGranter refreshTokenGranter) {
        return addGranter(GrantType.refresh_token, RefreshTokenRequest.class, refreshTokenGranter::refreshToken);
    }

    public DefaultOAuth2Granter addClientCredentialSupport(ClientCredentialGranter clientCredentialGranter) {
        return addGranter(GrantType.client_credentials, ClientCredentialRequest.class, clientCredentialGranter::requestToken);
    }

    public DefaultOAuth2Granter addPasswordSupport(PasswordGranter passwordGranter) {
        return addGranter(GrantType.password, PasswordRequest.class, passwordGranter::requestToken);
    }

    public DefaultOAuth2Granter addImplicitSupport(ImplicitGranter implicitGranter) {
        return addGranter(GrantType.implicit, ImplicitRequest.class, implicitGranter::requestToken);
    }

    private <R extends TokenRequest> DefaultOAuth2Granter addGranter(String grantType, Class<R> tokenRequestType, Function<R, OAuth2AccessToken> granterService) {
        supportGranter.put(grantType, Granter.build(tokenRequestType, granterService));
        return this;
    }

    @Override
    public OAuth2AccessToken grant(String grantType, TokenRequest request) {
        assertParameterNotBlank(grantType, ILLEGAL_GRANT_TYPE);
        Granter granter = supportGranter.get(grantType);
        if (granter == null) {
            throw new GrantTokenException(UNSUPPORTED_GRANT_TYPE);
        }
        return granter.grant(request);
    }

    private void assertParameterNotBlank(String parameter, ErrorType type) {
        if (null == parameter || parameter.isEmpty()) {
            throw new GrantTokenException(type);
        }
    }

    static class Granter<R extends TokenRequest> {
        Class<R> tokenRequestType;

        Function<R, OAuth2AccessToken> granterService;

        OAuth2AccessToken grant(TokenRequest request) {
            if (!tokenRequestType.isInstance(request)) {
                throw new UnsupportedOperationException("AuthorizationRequest must instanceof  " + tokenRequestType);
            }
            return granterService.apply(tokenRequestType.cast(request));
        }

        static <R extends TokenRequest> Granter<R> build(Class<R> tokenRequestType, Function<R, OAuth2AccessToken> granterService) {
            Granter<R> granter = new Granter<>();
            granter.tokenRequestType = tokenRequestType;
            granter.granterService = granterService;
            return granter;
        }

    }

}
