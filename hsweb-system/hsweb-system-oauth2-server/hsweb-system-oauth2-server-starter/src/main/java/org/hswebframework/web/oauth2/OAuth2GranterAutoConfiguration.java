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

package org.hswebframework.web.oauth2;

import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.oauth2.server.client.OAuth2ClientConfigRepository;
import org.hswebframework.web.authorization.oauth2.server.support.AbstractAuthorizationService;
import org.hswebframework.web.authorization.oauth2.server.support.DefaultOAuth2Granter;
import org.hswebframework.web.authorization.oauth2.server.support.client.ClientCredentialGranter;
import org.hswebframework.web.authorization.oauth2.server.support.client.DefaultClientCredentialGranter;
import org.hswebframework.web.authorization.oauth2.server.support.code.AuthorizationCodeGranter;
import org.hswebframework.web.authorization.oauth2.server.support.code.AuthorizationCodeService;
import org.hswebframework.web.authorization.oauth2.server.support.code.DefaultAuthorizationCodeGranter;
import org.hswebframework.web.authorization.oauth2.server.support.implicit.DefaultImplicitGranter;
import org.hswebframework.web.authorization.oauth2.server.support.implicit.ImplicitGranter;
import org.hswebframework.web.authorization.oauth2.server.support.password.DefaultPasswordGranter;
import org.hswebframework.web.authorization.oauth2.server.support.password.PasswordGranter;
import org.hswebframework.web.authorization.oauth2.server.support.password.PasswordService;
import org.hswebframework.web.authorization.oauth2.server.support.refresh.DefaultRefreshTokenGranter;
import org.hswebframework.web.authorization.oauth2.server.support.refresh.RefreshTokenGranter;
import org.hswebframework.web.authorization.oauth2.server.token.AccessTokenService;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.dao.oauth2.server.AuthorizationCodeDao;
import org.hswebframework.web.dao.oauth2.server.OAuth2AccessDao;
import org.hswebframework.web.dao.oauth2.server.OAuth2ClientDao;
import org.hswebframework.web.service.oauth2.server.simple.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * @author zhouhao
 */
@Configuration
@ComponentScan({"org.hswebframework.web.service.oauth2.server.simple"
        , "org.hswebframework.web.authorization.oauth2.controller"})
public class OAuth2GranterAutoConfiguration {

    @Autowired(required = false)
    private CodeGenerator codeGenerator;

    @Autowired(required = false)
    private TokenGenerator tokenGenerator;

    @ConditionalOnMissingBean(AuthorizationCodeService.class)
    @Bean
    public SimpleAuthorizationCodeService simpleAuthorizationCodeService(AuthorizationCodeDao authorizationCodeDao,
                                                                         EntityFactory entityFactory) {
        return new SimpleAuthorizationCodeService(authorizationCodeDao, entityFactory)
                .setCodeGenerator(codeGenerator);
    }

    @ConditionalOnMissingBean(OAuth2ClientConfigRepository.class)
    @Bean
    public SimpleClientConfigRepository simpleClientService(OAuth2ClientDao oAuth2ClientDao) {
        return new SimpleClientConfigRepository(oAuth2ClientDao);
    }

    @ConditionalOnMissingBean(PasswordService.class)
    @Bean
    public SimplePasswordService simplePasswordService(AuthenticationManager userService) {
        return new SimplePasswordService(userService);
    }

    @ConditionalOnMissingBean(AccessTokenService.class)
    @Bean
    public SimpleAccessTokenService simpleAccessTokenService(OAuth2AccessDao oAuth2AccessDao, EntityFactory entityFactory) {
        return new SimpleAccessTokenService(oAuth2AccessDao, entityFactory)
                .setTokenGenerator(tokenGenerator);
    }

    @Bean
    @ConditionalOnBean(UserTokenManager.class)
    public OAuth2GrantEventListener oAuth2GrantEventListener(UserTokenManager userTokenManager) {
        return new OAuth2GrantEventListener(userTokenManager);
    }

    @Configuration
    public static class OAuth2GranterConfiguration {
        @Autowired
        private AuthorizationCodeService     authorizationCodeService;
        @Autowired
        private OAuth2ClientConfigRepository oAuth2ClientConfigRepository;
        @Autowired
        private AccessTokenService           accessTokenService;
        @Autowired
        private PasswordService              passwordService;

        private <T extends AbstractAuthorizationService> T setProperty(T abstractAuthorizationService) {
            abstractAuthorizationService.setAccessTokenService(accessTokenService);
            abstractAuthorizationService.setRepository(oAuth2ClientConfigRepository);
            return abstractAuthorizationService;
        }

        @Bean
        @ConditionalOnMissingBean(AuthorizationCodeGranter.class)
        public AuthorizationCodeGranter authorizationCodeGranter() {
            return setProperty(new DefaultAuthorizationCodeGranter(authorizationCodeService));
        }

        @Bean
        @ConditionalOnMissingBean(ClientCredentialGranter.class)
        public ClientCredentialGranter clientCredentialGranter() {
            return setProperty(new DefaultClientCredentialGranter());
        }

        @Bean
        @ConditionalOnMissingBean(PasswordGranter.class)
        public PasswordGranter passwordGranter() {
            return setProperty(new DefaultPasswordGranter(passwordService));
        }

        @Bean
        @ConditionalOnMissingBean(ImplicitGranter.class)
        public ImplicitGranter implicitGranter() {
            return setProperty(new DefaultImplicitGranter());
        }

        @Bean
        @ConditionalOnMissingBean(RefreshTokenGranter.class)
        @ConfigurationProperties(prefix = "hsweb.oauth2.server")
        public RefreshTokenGranter refreshTokenGranter() {
            return setProperty(new DefaultRefreshTokenGranter());
        }
    }

    @Bean
    public AutoSettingOAuth2Granter autoSettingOAuth2Granter() {
        return new AutoSettingOAuth2Granter();
    }

    class AutoSettingOAuth2Granter extends DefaultOAuth2Granter implements BeanPostProcessor {
        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof AuthorizationCodeGranter) {
                addAuthorizationCodeSupport(((AuthorizationCodeGranter) bean));
            }
            if (bean instanceof ClientCredentialGranter) {
                addClientCredentialSupport(((ClientCredentialGranter) bean));
            }
            if (bean instanceof PasswordGranter) {
                addPasswordSupport(((PasswordGranter) bean));
            }
            if (bean instanceof ImplicitGranter) {
                addImplicitSupport(((ImplicitGranter) bean));
            }
            if (bean instanceof RefreshTokenGranter) {
                addRefreshTokenSupport(((RefreshTokenGranter) bean));
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }
    }

}
