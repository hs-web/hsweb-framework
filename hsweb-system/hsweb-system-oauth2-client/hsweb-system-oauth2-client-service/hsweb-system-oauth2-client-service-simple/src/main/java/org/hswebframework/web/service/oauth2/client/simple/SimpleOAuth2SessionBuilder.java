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

package org.hswebframework.web.service.oauth2.client.simple;

import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.oauth2.client.*;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Session;
import org.hswebframework.web.entity.oauth2.client.OAuth2ServerConfigEntity;
import org.hswebframework.web.entity.oauth2.client.OAuth2UserTokenEntity;
import org.hswebframework.web.service.oauth2.client.OAuth2UserTokenService;
import org.hswebframework.web.service.oauth2.client.simple.session.AuthorizationCodeSession;
import org.hswebframework.web.service.oauth2.client.simple.session.CachedOAuth2Session;
import org.hswebframework.web.service.oauth2.client.simple.session.DefaultOAuth2Session;
import org.hswebframework.web.service.oauth2.client.simple.session.PasswordSession;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * @author zhouhao
 */
public class SimpleOAuth2SessionBuilder implements OAuth2SessionBuilder {
    private OAuth2UserTokenService oAuth2UserTokenService;

    private OAuth2ServerConfigEntity configEntity;

    private OAuth2RequestBuilderFactory requestBuilderFactory;

    public SimpleOAuth2SessionBuilder(OAuth2UserTokenService oAuth2UserTokenService,
                                      OAuth2ServerConfigEntity oAuth2ServerConfig,
                                      OAuth2RequestBuilderFactory requestBuilderFactory) {
        this.oAuth2UserTokenService = oAuth2UserTokenService;
        this.configEntity = oAuth2ServerConfig;
        this.requestBuilderFactory = requestBuilderFactory;
    }

    protected String getRealUrl(String url) {
        if (url.startsWith("http")) return url;
        if (!configEntity.getApiBaseUrl().endsWith("/") && !url.startsWith("/"))
            return configEntity.getApiBaseUrl().concat("/").concat(url);
        return configEntity.getApiBaseUrl() + url;
    }

    private void token2entity(AccessTokenInfo token, OAuth2UserTokenEntity entity) {
        entity.setAccessToken(token.getAccessToken());
        entity.setRefreshToken(token.getRefreshToken());
        entity.setExpiresIn(token.getExpiresIn());
        entity.setScope(token.getScope());
        entity.setCreateTime(token.getCreateTime());
        entity.setUpdateTime(token.getUpdateTime());
    }

    private void entity2token(OAuth2UserTokenEntity entity, AccessTokenInfo token) {
        token.setAccessToken(entity.getAccessToken());
        token.setRefreshToken(entity.getRefreshToken());
        token.setExpiresIn(entity.getExpiresIn());
        token.setScope(entity.getScope());
        token.setCreateTime(entity.getCreateTime());
        token.setUpdateTime(entity.getUpdateTime());
    }


    protected OAuth2UserTokenEntity getClientCredentialsToken() {
        List<OAuth2UserTokenEntity> list = oAuth2UserTokenService
                .selectByServerIdAndGrantType(configEntity.getId(), GrantType.client_credentials);
        return list.isEmpty() ? null : list.get(0);
    }

    protected Consumer<AccessTokenInfo> createOnTokenChanged(Supplier<OAuth2UserTokenEntity> tokenGetter, String grantType) {
        return token -> {
            OAuth2UserTokenEntity tokenEntity = tokenGetter.get();
            if (tokenEntity != null) {
                tokenEntity.setUpdateTime(System.currentTimeMillis());
                token2entity(token, tokenEntity);
                oAuth2UserTokenService.updateByPk(tokenEntity.getId(), tokenEntity);
            } else {
                tokenEntity = oAuth2UserTokenService.createEntity();
                tokenEntity.setGrantType(grantType);
                tokenEntity.setCreateTime(System.currentTimeMillis());
                tokenEntity.setServerId(configEntity.getId());
                token2entity(token, tokenEntity);
                oAuth2UserTokenService.insert(tokenEntity);
            }
        };
    }

    private final Consumer<AccessTokenInfo> onClientCredentialsTokenChanged = createOnTokenChanged(this::getClientCredentialsToken, GrantType.client_credentials);

    @Override
    public OAuth2Session byAuthorizationCode(String code) {
        AuthorizationCodeSession authorizationCodeSession = new AuthorizationCodeSession();
        authorizationCodeSession.setCode(code);
        authorizationCodeSession.setRequestBuilderFactory(requestBuilderFactory);
        authorizationCodeSession.setConfigEntity(configEntity);
        authorizationCodeSession.init();
        return authorizationCodeSession;
    }


    @Override
    public OAuth2Session byClientCredentials() {
        OAuth2UserTokenEntity entity = getClientCredentialsToken();
        DefaultOAuth2Session session;
        if (null != entity) {
            AccessTokenInfo tokenInfo = new AccessTokenInfo();
            entity2token(entity, tokenInfo);
            session = new CachedOAuth2Session(tokenInfo);
        } else {
            session = new DefaultOAuth2Session();
        }
        session.setConfigEntity(configEntity);
        session.setRequestBuilderFactory(requestBuilderFactory);
        session.onTokenChanged(onClientCredentialsTokenChanged);
        session.init();
        session.param(OAuth2Constants.grant_type, GrantType.client_credentials);
        return session;
    }

    @Override
    public OAuth2Session byPassword(String username, String password) {
        PasswordSession session = new PasswordSession(username, password);
        session.setConfigEntity(configEntity);
        session.setRequestBuilderFactory(requestBuilderFactory);
        session.init();
        return session;
    }

    @Override
    public OAuth2Session byAccessToken(String accessToken) {
        Supplier<OAuth2UserTokenEntity> supplier = () -> oAuth2UserTokenService.selectByAccessToken(accessToken);
        OAuth2UserTokenEntity tokenEntity = supplier.get();
        if (tokenEntity == null) throw new NotFoundException("access_token not found");

        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        entity2token(tokenEntity, tokenInfo);
        CachedOAuth2Session session = new CachedOAuth2Session(tokenInfo);
        session.setConfigEntity(configEntity);
        session.setRequestBuilderFactory(requestBuilderFactory);
        session.onTokenChanged(createOnTokenChanged(supplier, null));
        session.init();
        return session;
    }


}
