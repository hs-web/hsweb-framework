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

package org.hswebframework.web.authorization.oauth2.client.simple;

import org.hswebframework.utils.ClassUtils;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestBuilderFactory;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.authorization.oauth2.client.OAuth2ServerConfig;
import org.hswebframework.web.authorization.oauth2.client.OAuth2SessionBuilder;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2Event;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2Listener;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.concurrent.lock.LockManager;

import java.util.*;

/**
 * @author zhouhao
 */
public class SimpleOAuth2RequestService implements OAuth2RequestService {

    private OAuth2ServerConfigRepository oAuth2ServerConfigService;

    private OAuth2UserTokenRepository oAuth2UserTokenService;

    private OAuth2RequestBuilderFactory oAuth2RequestBuilderFactory;

    private Map<String, Map<Class, List<OAuth2Listener>>> listenerStore = new HashMap<>();

    private LockManager lockManager;

    public SimpleOAuth2RequestService(
            OAuth2ServerConfigRepository oAuth2ServerConfigService
            , OAuth2UserTokenRepository oAuth2UserTokenService
            , OAuth2RequestBuilderFactory oAuth2RequestBuilderFactory
            , LockManager lockManager) {
        this.oAuth2ServerConfigService = oAuth2ServerConfigService;
        this.oAuth2UserTokenService = oAuth2UserTokenService;
        this.oAuth2RequestBuilderFactory = oAuth2RequestBuilderFactory;
        this.lockManager = lockManager;
    }

    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    @Override
    public OAuth2SessionBuilder create(String serverId) {
        OAuth2ServerConfig configEntity = oAuth2ServerConfigService.findById(serverId);
        if (null == configEntity || !DataStatus.STATUS_ENABLED.equals(configEntity.getStatus())) {
            throw new NotFoundException("server not found!");
        }
        return new SimpleOAuth2SessionBuilder(oAuth2UserTokenService, configEntity, oAuth2RequestBuilderFactory,
                lockManager.getReadWriteLock("oauth2-server-lock." + serverId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerListener(String serverId, OAuth2Listener<? extends OAuth2Event> listener) {
        Class type = ClassUtils.getGenericType(listener.getClass());
        listenerStore.computeIfAbsent(serverId, k -> new HashMap<>())
                .computeIfAbsent(type, k -> new ArrayList<>())
                .add(listener);
    }

    @Override
    public void doEvent(String serverId, OAuth2Event event) {
        doEvent(serverId, event, event.getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doEvent(String serverId, OAuth2Event event, Class<? extends OAuth2Event> eventType) {
        listenerStore.getOrDefault(serverId, new java.util.HashMap<>())
                .getOrDefault(eventType, new ArrayList<>())
                .forEach(listener -> listener.on(event));
    }

}
