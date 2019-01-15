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

package org.hswebframework.web.authorization.starter;

import org.hswebframework.web.authorization.AuthenticationInitializeService;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.basic.embed.EmbedAuthenticationManager;
import org.hswebframework.web.authorization.setting.UserSettingManager;
import org.hswebframework.web.authorization.simple.DefaultAuthorizationAutoConfiguration;
import org.hswebframework.web.authorization.twofactor.TwoFactorTokenManager;
import org.hswebframework.web.authorization.twofactor.defaults.HashMapTwoFactorTokenManager;
import org.hswebframework.web.service.authorization.simple.SimpleAuthenticationManager;
import org.hswebframework.web.service.authorization.simple.totp.TotpTwoFactorProvider;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author zhouhao
 */
@Configuration
@ComponentScan({"org.hswebframework.web.service.authorization.simple"
        , "org.hswebframework.web.authorization.controller"})
@MapperScan("org.hswebframework.web.authorization.dao")
@AutoConfigureBefore(value = {
        DefaultAuthorizationAutoConfiguration.class
}, name = "org.hswebframework.web.authorization.basic.configuration.AuthorizingHandlerAutoConfiguration")
public class AuthorizationAutoConfiguration {

    @ConditionalOnMissingClass("org.hswebframework.web.authorization.basic.embed.EmbedAuthenticationManager")
    @Configuration
    public static class NoEmbedAuthenticationManagerAutoConfiguration {
        @Bean
        @Primary
        public AuthenticationManager authenticationManager(AuthenticationInitializeService authenticationInitializeService) {
            return new SimpleAuthenticationManager(authenticationInitializeService);
        }

    }

    @ConditionalOnClass(EmbedAuthenticationManager.class)
    @Configuration
    public static class EmbedAuthenticationManagerAutoConfiguration {
        @Bean
        public EmbedAuthenticationManager embedAuthenticationManager() {
            return new EmbedAuthenticationManager();
        }

        @Bean
        @Primary
        public AuthenticationManager authenticationManager(EmbedAuthenticationManager embedAuthenticationManager,
                                                           AuthenticationInitializeService authenticationInitializeService) {
            return new SimpleAuthenticationManager(authenticationInitializeService, embedAuthenticationManager);
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "hsweb.authorize", name = "sync", havingValue = "true")
    public AutoSyncPermission autoSyncPermission() {
        return new AutoSyncPermission();
    }

    @Bean
    @ConditionalOnMissingBean(TwoFactorTokenManager.class)
    public TwoFactorTokenManager twoFactorTokenManager() {
        return new HashMapTwoFactorTokenManager();
    }

    @Bean
    @ConditionalOnProperty(prefix = "hsweb.authorize.two-factor.totp", name = "enable", havingValue = "true")
    @ConfigurationProperties(prefix = "hsweb.authorize.two-factor.totp")
    public TotpTwoFactorProvider totpTwoFactorProvider(UserSettingManager userSettingManager,
                                                       TwoFactorTokenManager twoFactorTokenManager) {
        return new TotpTwoFactorProvider(userSettingManager, twoFactorTokenManager);
    }
}
