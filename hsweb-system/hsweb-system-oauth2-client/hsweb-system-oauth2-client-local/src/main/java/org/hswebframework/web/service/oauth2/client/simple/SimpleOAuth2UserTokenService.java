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
 */
package org.hswebframework.web.service.oauth2.client.simple;

import org.hswebframework.web.authorization.oauth2.client.AccessTokenInfo;
import org.hswebframework.web.authorization.oauth2.client.simple.OAuth2UserTokenRepository;
import org.hswebframework.web.dao.oauth2.server.client.OAuth2UserTokenDao;
import org.hswebframework.web.entity.oauth2.client.OAuth2UserTokenEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.oauth2.client.OAuth2UserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("oAuth2UserTokenService")
public class SimpleOAuth2UserTokenService extends GenericEntityService<OAuth2UserTokenEntity, String>
        implements OAuth2UserTokenService, OAuth2UserTokenRepository {
    @Autowired
    private OAuth2UserTokenDao oAuth2UserTokenDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public OAuth2UserTokenDao getDao() {
        return oAuth2UserTokenDao;
    }

    @Override
    public AccessTokenInfo createToken() {
        return entityFactory.newInstance(AccessTokenInfo.class);
    }

    @Override
    @Cacheable(cacheNames = "oauth2-user-token-list", key = "'s-g-t:'+#serverId+':'+#grantType")
    public List<AccessTokenInfo> findByServerIdAndGrantType(String serverId, String grantType) {
        return selectByServerIdAndGrantType(serverId, grantType).stream()
                .map(tokenInfoMapping())
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = "oauth2-user-token", key = "'a-t:'+#accessToken")
    public AccessTokenInfo findByAccessToken(String accessToken) {
        return Optional.ofNullable(selectByAccessToken(accessToken)).map(tokenInfoMapping()).orElse(null);
    }

    protected Function<OAuth2UserTokenEntity, AccessTokenInfo> tokenInfoMapping() {
        return entity -> {

            AccessTokenInfo info = entityFactory.newInstance(AccessTokenInfo.class, entity);
            info.setExpiresIn(entity.getExpiresIn());
            info.setAccessToken(entity.getAccessToken());
            info.setCreateTime(entity.getCreateTime());
            info.setUpdateTime(entity.getUpdateTime());
            info.setRefreshToken(entity.getRefreshToken());
            info.setServerId(entity.getServerId());
            info.setGrantType(entity.getGrantType());
            info.setScope(entity.getScope());
            return info;
        };
    }

    protected Function<AccessTokenInfo, OAuth2UserTokenEntity> entityTokenInfoMapping() {
        return info ->
        {
            OAuth2UserTokenEntity entity = entityFactory.newInstance(OAuth2UserTokenEntity.class, info);
            entity.setExpiresIn(info.getExpiresIn());
            entity.setAccessToken(info.getAccessToken());
            entity.setCreateTime(info.getCreateTime());
            entity.setUpdateTime(info.getUpdateTime());
            entity.setRefreshToken(info.getRefreshToken());
            entity.setServerId(info.getServerId());
            entity.setGrantType(info.getGrantType());
            entity.setScope(info.getScope());
            return entity;
        };
    }

    @Override
    @Caching(
            put = @CachePut(cacheNames = "oauth2-user-token", key = "'a-t:'+#tokenInfo.accessToken"),
            evict = @CacheEvict(cacheNames = "oauth2-user-token-list", key = "'s-g-t:'+#result.serverId+':'+#result.grantType")
    )
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AccessTokenInfo update(String id, AccessTokenInfo tokenInfo) {
        OAuth2UserTokenEntity entity = entityTokenInfoMapping().apply(tokenInfo);
        entity.setUpdateTime(System.currentTimeMillis());
        updateByPk(id, entity);
        return tokenInfo;
    }

    @Override
    @Caching(
            put = @CachePut(cacheNames = "oauth2-user-token", key = "'a-t:'+#tokenInfo.accessToken"),
            evict = @CacheEvict(cacheNames = "oauth2-user-token-list", key = "'s-g-t:'+#result.serverId+':'+#result.grantType")
    )
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AccessTokenInfo insert(AccessTokenInfo tokenInfo) {
        if (tokenInfo.getId() == null) {
            tokenInfo.setId(getIDGenerator().generate());
        }
        OAuth2UserTokenEntity entity = entityTokenInfoMapping().apply(tokenInfo);
        entity.setCreateTime(tokenInfo.getCreateTime());
        entity.setUpdateTime(tokenInfo.getUpdateTime());

        insert(entity);
        return tokenInfo;
    }

    public List<OAuth2UserTokenEntity> selectByServerIdAndGrantType(String serverId, String grantType) {
        Assert.notNull(serverId, "serverId can not be null!");
        Assert.notNull(grantType, "grantType can not be null!");
        return createQuery()
                .where(OAuth2UserTokenEntity.serverId, serverId)
                .is(OAuth2UserTokenEntity.grantType, grantType)
                .listNoPaging();
    }

    public OAuth2UserTokenEntity selectByAccessToken(String accessToken) {
        Assert.notNull(accessToken, "token can not be null!");
        return createQuery().where(OAuth2UserTokenEntity.accessToken, accessToken)
                .single();
    }
}
