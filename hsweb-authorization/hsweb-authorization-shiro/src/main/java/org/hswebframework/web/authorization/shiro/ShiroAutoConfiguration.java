/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.authorization.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.hswebframework.web.authorization.AuthenticationHolder;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.AuthenticationSupplier;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.shiro.boost.BoostAuthorizationAttributeSourceAdvisor;
import org.hswebframework.web.authorization.shiro.cache.SpringCacheManagerWrapper;
import org.hswebframework.web.authorization.shiro.remember.SimpleRememberMeManager;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zhouhao
 */
@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class ShiroAutoConfiguration {

    @Autowired(required = false)
    private org.springframework.cache.CacheManager cacheManager;

    @Bean
    public CacheManager shiroCacheManager() {
        if (cacheManager == null) {
            return new MemoryConstrainedCacheManager();
        } else {
            return new SpringCacheManagerWrapper(cacheManager);
        }
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public ListenerAuthorizingRealm listenerAuthorizingRealm(CacheManager cacheManager) {
        ListenerAuthorizingRealm realm = new ListenerAuthorizingRealm();
        realm.setCacheManager(cacheManager);
        return realm;
    }

    @Bean
    @ConditionalOnMissingBean
    public AutoSyncAuthenticationSupplier authorizationSupplier(AuthenticationManager authenticationManager) {
        AutoSyncAuthenticationSupplier syncAuthenticationSupplier = new AutoSyncAuthenticationSupplier(authenticationManager);
        return syncAuthenticationSupplier;
    }

    @Bean
    public LoginExitListener loginExitListener(ListenerAuthorizingRealm listenerAuthorizingRealm) {
        return new LoginExitListener(listenerAuthorizingRealm);
    }

    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor getLifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public BeanPostProcessor authenticationSupplierBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof AuthenticationSupplier) {
                    AuthenticationHolder.addSupplier(((AuthenticationSupplier) bean));
                }
                return bean;
            }
        };
    }

    @Bean(name = "securityManager")
    @ConditionalOnWebApplication
    public DefaultWebSecurityManager defaultWebSecurityManager(List<AuthorizingRealm> authorizingRealm,
                                                               CacheManager cacheManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealms(new ArrayList<>(authorizingRealm));
        securityManager.setCacheManager(cacheManager);
        securityManager.setRememberMeManager(new SimpleRememberMeManager());
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    @Bean(name = "securityManager")
    @ConditionalOnNotWebApplication
    public DefaultSecurityManager defaultSecurityManager(List<AuthorizingRealm> authorizingRealm,
                                                         CacheManager cacheManager) {
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        securityManager.setRealms(new ArrayList<>(authorizingRealm));
        securityManager.setCacheManager(cacheManager);
        securityManager.setSessionManager(new DefaultSessionManager());
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }


    @Bean
    public BoostAuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager,
                                                                                        DataAccessController dataAccessController) {
        BoostAuthorizationAttributeSourceAdvisor advisor = new BoostAuthorizationAttributeSourceAdvisor(dataAccessController);
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    @Configuration
    @EnableConfigurationProperties(ShiroProperties.class)
    @ConditionalOnProperty(prefix = "hsweb.authorize", name = "enable", havingValue = "true", matchIfMissing = true)
    static class FilterConfiguration {
        @Autowired
        private ShiroProperties shiroProperties;

        @Bean(name = "shiroFilter")
        public ShiroFilterFactoryBean shiroFilterFactoryBean(WebSecurityManager securityManager) {
            ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
            // 必须设置 SecurityManager
            shiroFilterFactoryBean.setSecurityManager(securityManager);
            if (null != shiroProperties)
                shiroFilterFactoryBean.setFilterChainDefinitionMap(shiroProperties.getFilters());
            else
                shiroFilterFactoryBean.setFilterChainDefinitionMap(Collections.emptyMap());
            shiroFilterFactoryBean.setSuccessUrl(shiroProperties.getSuccessUrl());
            shiroFilterFactoryBean.setLoginUrl(shiroProperties.getLoginUrl());
            shiroFilterFactoryBean.setUnauthorizedUrl(shiroProperties.getUnauthorizedUrl());
            return shiroFilterFactoryBean;
        }
    }

    @RestControllerAdvice
    public static class UnAuthControllerAdvice {
        @ExceptionHandler(AuthorizationException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        @ResponseBody
        ResponseMessage handleException(AuthorizationException exception) {
            return ResponseMessage.error(403, exception.getMessage());
        }

        @ExceptionHandler(UnauthenticatedException.class)
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        @ResponseBody
        ResponseMessage handleException(UnauthenticatedException exception) {
            return ResponseMessage.error(401, exception.getMessage() == null ? "{access_denied}" : exception.getMessage());
        }
    }

}
