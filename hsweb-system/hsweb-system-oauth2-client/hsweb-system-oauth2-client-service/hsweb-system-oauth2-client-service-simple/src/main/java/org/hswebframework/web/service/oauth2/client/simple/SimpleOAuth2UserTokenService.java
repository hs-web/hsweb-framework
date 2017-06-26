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
 */
package org.hswebframework.web.service.oauth2.client.simple;

import org.hswebframework.web.dao.oauth2.client.OAuth2UserTokenDao;
import org.hswebframework.web.entity.oauth2.client.OAuth2UserTokenEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.oauth2.client.OAuth2UserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("oAuth2UserTokenService")
public class SimpleOAuth2UserTokenService extends GenericEntityService<OAuth2UserTokenEntity, String>
        implements OAuth2UserTokenService {
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
    @Cacheable(cacheNames = "oauth2-user-token", key = "'s-g-t:'+#serverId+':'+#grantType")
    public List<OAuth2UserTokenEntity> selectByServerIdAndGrantType(String serverId, String grantType) {
        Assert.notNull(serverId, "serverId can not be null!");
        Assert.notNull(grantType, "grantType can not be null!");
        return createQuery()
                .where(OAuth2UserTokenEntity.serverId, serverId)
                .is(OAuth2UserTokenEntity.grantType, grantType)
                .listNoPaging();
    }

    @Override
    @Cacheable(cacheNames = "oauth2-user-token", key = "'a-t:'+#serverId+':'+#grantType")
    public OAuth2UserTokenEntity selectByAccessToken(String accessToken) {
        Assert.notNull(accessToken, "token can not be null!");
        return createQuery().where(OAuth2UserTokenEntity.accessToken, accessToken)
                .single();
    }
}
