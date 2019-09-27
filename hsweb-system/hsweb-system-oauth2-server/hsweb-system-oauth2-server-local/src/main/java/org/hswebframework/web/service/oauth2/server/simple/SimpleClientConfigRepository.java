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

package org.hswebframework.web.service.oauth2.server.simple;

import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.web.authorization.oauth2.server.client.OAuth2Client;
import org.hswebframework.web.authorization.oauth2.server.client.OAuth2ClientConfigRepository;
import org.hswebframework.web.entity.oauth2.server.OAuth2ClientEntity;
import org.hswebframework.web.entity.oauth2.server.SimpleOAuth2ClientEntity;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.springframework.cache.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
@CacheConfig(cacheNames = "oauth2-client-config")
public class SimpleClientConfigRepository implements OAuth2ClientConfigRepository {
    private SyncRepository<OAuth2ClientEntity, String> oAuth2ClientDao;

    public SimpleClientConfigRepository(SyncRepository<OAuth2ClientEntity, String> oAuth2ClientDao) {
        this.oAuth2ClientDao = oAuth2ClientDao;
    }

    @Override
    @Cacheable(key = "'id:'+#id")
    public OAuth2Client getClientById(String id) {
        return DefaultDSLQueryService
                .createQuery(oAuth2ClientDao)
                .where("id", id)
                .fetchOne()
                .orElse(null);
    }

    @Override
    @Cacheable(key = "'ownerId:'+#ownerId")
    public OAuth2Client getClientByOwnerId(String ownerId) {
        return DefaultDSLQueryService
                .createQuery(oAuth2ClientDao).where("ownerId", ownerId)
                .fetchOne().orElse(null);
    }

    @Override
    @Caching(put = {
            @CachePut(key = "'ownerId:'+#result.ownerId"),
            @CachePut(key = "'id:'+#result.id")
    })
    public OAuth2Client save(OAuth2Client oAuth2Client) {
        OAuth2Client old = getClientById(oAuth2Client.getId());
        if (old != null) {
            oAuth2ClientDao.createUpdate()
                    .set((OAuth2ClientEntity) oAuth2Client)
                    .excludes("id", "createTime")
                    .where("id", oAuth2Client.getId())
                    .execute();

        } else {
            oAuth2ClientDao.insert(((SimpleOAuth2ClientEntity) oAuth2Client));
        }
        return oAuth2Client;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'ownerId:'+#result.ownerId", condition = "#result!=null"),
            @CacheEvict(key = "'id:'+#result.id", condition = "#result!=null")
    })
    public OAuth2Client remove(String id) {
        OAuth2Client old = getClientById(id);
        oAuth2ClientDao.deleteById(id);
        return old;
    }

    @Override
    public OAuth2Client newClient() {
        SimpleOAuth2ClientEntity clientEntity = SimpleOAuth2ClientEntity.builder()
                .build();
        clientEntity.setId(IDGenerator.MD5.generate());
        clientEntity.setSecret(IDGenerator.MD5.generate());
        clientEntity.setStatus(DataStatus.STATUS_ENABLED);
        clientEntity.setCreateTimeNow();
        return clientEntity;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OAuth2Client> getAll() {
        return oAuth2ClientDao
                .createQuery()
                .fetch()
                .stream()
                .map(OAuth2Client.class::cast)
                .collect(Collectors.toList());
    }
}
