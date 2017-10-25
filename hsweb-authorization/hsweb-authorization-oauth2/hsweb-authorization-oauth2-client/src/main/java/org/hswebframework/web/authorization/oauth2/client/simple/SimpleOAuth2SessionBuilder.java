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

package org.hswebframework.web.authorization.oauth2.client.simple;

import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.oauth2.client.*;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Session;
import org.hswebframework.web.authorization.oauth2.client.simple.session.AuthorizationCodeSession;
import org.hswebframework.web.authorization.oauth2.client.simple.session.CachedOAuth2Session;
import org.hswebframework.web.authorization.oauth2.client.simple.session.DefaultOAuth2Session;
import org.hswebframework.web.authorization.oauth2.client.simple.session.PasswordSession;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * @author zhouhao
 */
public class SimpleOAuth2SessionBuilder implements OAuth2SessionBuilder {
    private OAuth2UserTokenRepository oAuth2UserTokenRepository;

    private OAuth2ServerConfig serverConfig;

    private OAuth2RequestBuilderFactory requestBuilderFactory;

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public SimpleOAuth2SessionBuilder(OAuth2UserTokenRepository oAuth2UserTokenRepository,
                                      OAuth2ServerConfig oAuth2ServerConfig,
                                      OAuth2RequestBuilderFactory requestBuilderFactory) {
        this.oAuth2UserTokenRepository = oAuth2UserTokenRepository;
        this.serverConfig = oAuth2ServerConfig;
        this.requestBuilderFactory = requestBuilderFactory;
    }

    protected String getRealUrl(String url) {
        if (url.startsWith("http")) {
            return url;
        }
        if (!serverConfig.getApiBaseUrl().endsWith("/") && !url.startsWith("/")) {
            return serverConfig.getApiBaseUrl().concat("/").concat(url);
        }
        return serverConfig.getApiBaseUrl() + url;
    }


    protected AccessTokenInfo getClientCredentialsToken() {
        readWriteLock.readLock().lock();
        try {
            List<AccessTokenInfo> list = oAuth2UserTokenRepository
                    .findByServerIdAndGrantType(serverConfig.getId(), GrantType.client_credentials);
            return list.isEmpty() ? null : list.get(0);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    protected Consumer<AccessTokenInfo> createOnTokenChanged(Supplier<AccessTokenInfo> tokenGetter, String grantType) {
        return token -> {
            AccessTokenInfo tokenInfo = tokenGetter.get();
            readWriteLock.writeLock().lock();
            try {
                if (tokenInfo != null) {
                    token.setId(tokenInfo.getId());
                    tokenInfo.setUpdateTime(System.currentTimeMillis());
                    oAuth2UserTokenRepository.update(tokenInfo.getId(), token);
                } else {
                    token.setGrantType(grantType);
                    token.setCreateTime(System.currentTimeMillis());
                    token.setServerId(serverConfig.getId());
                    oAuth2UserTokenRepository.insert(token);
                }
            } finally {
                readWriteLock.writeLock().unlock();
            }
        };
    }

    private final Consumer<AccessTokenInfo> onClientCredentialsTokenChanged = createOnTokenChanged(this::getClientCredentialsToken, GrantType.client_credentials);

    @Override
    public OAuth2Session byAuthorizationCode(String code) {
        AuthorizationCodeSession authorizationCodeSession = new AuthorizationCodeSession();
        authorizationCodeSession.setCode(code);
        authorizationCodeSession.setRequestBuilderFactory(requestBuilderFactory);
        authorizationCodeSession.setServerConfig(serverConfig);
        authorizationCodeSession.init();
        return authorizationCodeSession;
    }


    @Override
    public OAuth2Session byClientCredentials() {
        AccessTokenInfo tokenInfo = getClientCredentialsToken();
        DefaultOAuth2Session session;
        if (null != tokenInfo) {
            session = new CachedOAuth2Session(tokenInfo);
        } else {
            session = new DefaultOAuth2Session();
        }
        session.setServerConfig(serverConfig);
        session.setRequestBuilderFactory(requestBuilderFactory);
        session.onTokenChanged(onClientCredentialsTokenChanged);
        session.init();
        session.param(OAuth2Constants.grant_type, GrantType.client_credentials);
        return session;
    }

    @Override
    public OAuth2Session byPassword(String username, String password) {
        PasswordSession session = new PasswordSession(username, password);
        session.setServerConfig(serverConfig);
        session.setRequestBuilderFactory(requestBuilderFactory);
        session.init();
        return session;
    }

    @Override
    public OAuth2Session byAccessToken(String accessToken) {
        Supplier<AccessTokenInfo> supplier = () -> oAuth2UserTokenRepository.findByAccessToken(accessToken);
        AccessTokenInfo tokenEntity = supplier.get();
        if (tokenEntity == null) {
            throw new NotFoundException("access_token not found");
        }
        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        CachedOAuth2Session session = new CachedOAuth2Session(tokenInfo);
        session.setServerConfig(serverConfig);
        session.setRequestBuilderFactory(requestBuilderFactory);
        session.onTokenChanged(createOnTokenChanged(supplier, null));
        session.init();
        return session;
    }


}
