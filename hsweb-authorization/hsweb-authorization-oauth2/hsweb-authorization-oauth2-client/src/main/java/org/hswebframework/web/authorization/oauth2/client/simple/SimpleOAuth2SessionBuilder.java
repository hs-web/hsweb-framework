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
import org.hswebframework.web.oauth2.core.GrantType;
import org.hswebframework.web.oauth2.core.OAuth2Constants;

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

    private ReadWriteLock readWriteLock;//.= new ReentrantReadWriteLock();


    public SimpleOAuth2SessionBuilder(OAuth2UserTokenRepository oAuth2UserTokenRepository,
                                      OAuth2ServerConfig oAuth2ServerConfig,
                                      OAuth2RequestBuilderFactory requestBuilderFactory,
                                      ReadWriteLock readWriteLock) {
        this.oAuth2UserTokenRepository = oAuth2UserTokenRepository;
        this.serverConfig = oAuth2ServerConfig;
        this.requestBuilderFactory = requestBuilderFactory;
        this.readWriteLock = readWriteLock;
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
        return oAuth2UserTokenRepository
                .findByServerIdAndGrantType(serverConfig.getId(), GrantType.client_credentials)
                .stream()
                .findAny()
                .orElse(null);
    }

    protected Consumer<AccessTokenInfo> createOnTokenChanged(Supplier<AccessTokenInfo> tokenGetter, String grantType) {
        return token -> {
            readWriteLock.writeLock().lock();
            AccessTokenInfo tokenInfo = tokenGetter.get();
            try {
                token.setGrantType(grantType);
                token.setServerId(serverConfig.getId());
                if (tokenInfo != null) {
                    token.setId(tokenInfo.getId());
                    token.setUpdateTime(System.currentTimeMillis());
                    oAuth2UserTokenRepository.update(tokenInfo.getId(), token);
                } else {
                    token.setCreateTime(System.currentTimeMillis());
                    token.setUpdateTime(System.currentTimeMillis());
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


    private Supplier<AccessTokenInfo> tokenGetter = () -> {
        readWriteLock.readLock().lock();
        try {
            return getClientCredentialsToken();
        } finally {
            readWriteLock.readLock().unlock();
        }
    };

    @Override
    public OAuth2Session byClientCredentials() {
        DefaultOAuth2Session session;

        AccessTokenInfo info = tokenGetter.get();

        if (null != info) {
            session = new CachedOAuth2Session(info);

        } else {
            readWriteLock.writeLock().lock();
            try {
                info = getClientCredentialsToken();
                if (null == info) {
                    session = new DefaultOAuth2Session();
                    session.setServerConfig(serverConfig);
                    session.setRequestBuilderFactory(requestBuilderFactory);
                    session.onTokenChanged(onClientCredentialsTokenChanged);
                    session.init();
                    session.param(OAuth2Constants.grant_type, GrantType.client_credentials);
                    info = session.requestAccessToken();
                    info.setGrantType(GrantType.client_credentials);
                    info.setCreateTime(System.currentTimeMillis());
                    info.setServerId(serverConfig.getId());
                    oAuth2UserTokenRepository.insert(info);
                }
            } finally {
                readWriteLock.writeLock().unlock();
            }
            session = new CachedOAuth2Session(info);
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
        AccessTokenInfo tokenInfo = supplier.get();
        if (tokenInfo == null) {
            throw new NotFoundException("access_token not found");
        }
        CachedOAuth2Session session = new CachedOAuth2Session(tokenInfo);
        session.setServerConfig(serverConfig);
        session.setRequestBuilderFactory(requestBuilderFactory);
        session.onTokenChanged(createOnTokenChanged(supplier, null));
        session.init();
        return session;
    }

}
