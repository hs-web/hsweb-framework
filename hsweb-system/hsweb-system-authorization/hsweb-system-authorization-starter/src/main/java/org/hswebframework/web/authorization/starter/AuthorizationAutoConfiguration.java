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
import org.hswebframework.web.authorization.simple.DefaultAuthorizationAutoConfiguration;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.service.authorization.simple.SimpleAuthenticationManager;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author zhouhao
 */
@Configuration
@ComponentScan({"org.hswebframework.web.service.authorization.simple"
        , "org.hswebframework.web.controller.authorization"})
@AutoConfigureBefore(DefaultAuthorizationAutoConfiguration.class)
@Import(BasicAuthorizationConfiguration.class)
public class AuthorizationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuthenticationManager.class)
    public AuthenticationManager authenticationManager(AuthenticationInitializeService authenticationInitializeService) {
        return new SimpleAuthenticationManager(authenticationInitializeService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "hsweb.authorize", name = "sync", havingValue = "true")
    public AutoSyncPermission autoSyncPermission() {
        return new AutoSyncPermission();
    }

}
