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

package org.hswebframework.web.authorization.starter;

import org.hswebframework.web.authorization.AuthenticationInitializeService;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.listener.AuthorizationListener;
import org.hswebframework.web.authorization.listener.AuthorizationListenerDispatcher;
import org.hswebframework.web.authorization.listener.event.AuthorizationEvent;
import org.hswebframework.web.service.authorization.simple.SimpleAuthenticationManager;
import org.hswebframework.utils.ClassUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author zhouhao
 */
@Configuration
public class AuthorizationAutoConfigration {

    @Autowired(required = false)
    private List<AuthorizationListener> listeners;

    @Bean
    @SuppressWarnings("unchecked")
    public <E extends AuthorizationEvent> AuthorizationListenerDispatcher authorizationListenerDispatcher() {
        AuthorizationListenerDispatcher dispatcher = new AuthorizationListenerDispatcher();
        if (listeners != null) {
            listeners.forEach(listener -> dispatcher.addListener((Class<E>) ClassUtils.getGenericType(listener.getClass()), listener));
        }
        return dispatcher;
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationManager.class)
    @ConditionalOnBean(AuthenticationInitializeService.class)
    public AuthenticationManager authenticationManager(AuthenticationInitializeService authenticationInitializeService) {
        return new SimpleAuthenticationManager(authenticationInitializeService);
    }
}
