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

package org.hswebframework.web.service.oauth2.server.simple;


import org.hswebframework.web.entity.oauth2.server.OAuth2AccessEntity;
import org.hswebframework.web.authorization.oauth2.server.OAuth2AccessToken;
import org.hswebframework.web.authorization.oauth2.server.token.AccessTokenService;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.dao.oauth2.server.OAuth2AccessDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.DefaultDSLUpdateService;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * @author zhouhao
 */
public class SimpleAccessTokenService implements AccessTokenService {

    private TokenGenerator tokenGenerator = IDGenerator.MD5::generate;

    private OAuth2AccessDao oAuth2AccessDao;

    private EntityFactory entityFactory;

    public SimpleAccessTokenService(OAuth2AccessDao oAuth2AccessDao, EntityFactory entityFactory) {
        this.oAuth2AccessDao = oAuth2AccessDao;
        this.entityFactory = entityFactory;
    }

    public SimpleAccessTokenService setTokenGenerator(TokenGenerator tokenGenerator) {
        if (tokenGenerator != null) {
            this.tokenGenerator = tokenGenerator;
        }
        return this;
    }

    @Override
    @Cacheable(cacheNames = "oauth2-access-token", key = "'cgo'+#token.clientId+#token.grantType+#token.ownerId")
    public OAuth2AccessToken tryGetOldToken(OAuth2AccessToken token) {
        OAuth2AccessToken old = DefaultDSLQueryService
                .createQuery(oAuth2AccessDao)
                .where("clientId", token.getClientId())
                .and("grantType", token.getGrantType())
                .and("ownerId", token.getOwnerId())
                .single();
        return old;
    }

    @Override
    public OAuth2AccessToken createToken() {
        OAuth2AccessEntity accessEntity = entityFactory.newInstance(OAuth2AccessEntity.class);
        accessEntity.setAccessToken(tokenGenerator.generate());
        accessEntity.setRefreshToken(tokenGenerator.generate());
        accessEntity.setCreateTime(System.currentTimeMillis());
        return accessEntity;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Cacheable(cacheNames = "oauth2-access-token", key = "'refresh:'+#refreshToken")
    public OAuth2AccessToken getTokenByRefreshToken(String refreshToken) {
        Assert.notNull(refreshToken, "refreshToken can not be null!");
        return DefaultDSLQueryService.createQuery(oAuth2AccessDao)
                .where("refreshToken", refreshToken).single();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Cacheable(cacheNames = "oauth2-access-token", key = "'token:'+#accessToken")
    public OAuth2AccessToken getTokenByAccessToken(String accessToken) {
        Assert.notNull(accessToken, "accessToken can not be null!");
        return DefaultDSLQueryService.createQuery(oAuth2AccessDao)
                .where("accessToken", accessToken).single();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Caching(put = {
            @CachePut(cacheNames = "oauth2-access-token", key = "'refresh:'+#result.refreshToken"),
            @CachePut(cacheNames = "oauth2-access-token", key = "'token:'+#result.accessToken"),
            @CachePut(cacheNames = "oauth2-access-token", key = "'cgo'+#result.clientId+#result.grantType+#result.ownerId")
    })
    public OAuth2AccessToken saveOrUpdateToken(OAuth2AccessToken token) {
        Assert.notNull(token, "token can not be null!");
        int total = DefaultDSLQueryService
                .createQuery(oAuth2AccessDao)
                .where("clientId", token.getClientId())
                .and("grantType", token.getGrantType())
                .and("ownerId", token.getOwnerId()).total();
        token.setUpdateTime(System.currentTimeMillis());
        if (total > 0) {
            DefaultDSLUpdateService
                    .createUpdate(oAuth2AccessDao, token)
                    .where("clientId", token.getClientId())
                    .and("grantType", token.getGrantType())
                    .and("ownerId", token.getOwnerId())
                    .exec();
        } else {
            token.setCreateTime(System.currentTimeMillis());
            oAuth2AccessDao.insert(((OAuth2AccessEntity) token));
        }

        return token;
    }
}
