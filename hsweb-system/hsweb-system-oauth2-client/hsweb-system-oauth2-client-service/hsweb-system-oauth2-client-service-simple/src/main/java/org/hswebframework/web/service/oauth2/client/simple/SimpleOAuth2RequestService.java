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
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestBuilderFactory;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.authorization.oauth2.client.OAuth2SessionBuilder;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2Event;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2Listener;
import org.hswebframework.web.entity.oauth2.client.OAuth2ServerConfigEntity;
import org.hswebframework.web.service.oauth2.client.OAuth2ServerConfigService;
import org.hswebframework.web.service.oauth2.client.OAuth2UserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Service("oAuth2RequestService")
public class SimpleOAuth2RequestService implements OAuth2RequestService {

    private OAuth2ServerConfigService oAuth2ServerConfigService;

    private OAuth2UserTokenService oAuth2UserTokenService;

    private OAuth2RequestBuilderFactory oAuth2RequestBuilderFactory;

    @Override
    public OAuth2SessionBuilder create(String serverId) {
        OAuth2ServerConfigEntity configEntity = oAuth2ServerConfigService.selectByPk(serverId);
        if (null == configEntity || !Boolean.TRUE.equals(configEntity.isEnabled())) throw new NotFoundException("server not found!");
        return new SimpleOAuth2SessionBuilder(oAuth2UserTokenService, configEntity, oAuth2RequestBuilderFactory);
    }

    @Override
    public void registerListener(String serverId, OAuth2Listener<? extends OAuth2Event> listener) {

    }

    @Override
    public void doEvent(String serverId, OAuth2Event event) {

    }

    @Override
    public void doEvent(String serverId, OAuth2Event event, Class<? extends OAuth2Event> eventType) {

    }

    @Autowired
    public void setoAuth2ServerConfigService(OAuth2ServerConfigService oAuth2ServerConfigService) {
        this.oAuth2ServerConfigService = oAuth2ServerConfigService;
    }

    @Autowired
    public void setoAuth2UserTokenService(OAuth2UserTokenService oAuth2UserTokenService) {
        this.oAuth2UserTokenService = oAuth2UserTokenService;
    }

    @Autowired
    public void setoAuth2RequestBuilderFactory(OAuth2RequestBuilderFactory oAuth2RequestBuilderFactory) {
        this.oAuth2RequestBuilderFactory = oAuth2RequestBuilderFactory;
    }
}
