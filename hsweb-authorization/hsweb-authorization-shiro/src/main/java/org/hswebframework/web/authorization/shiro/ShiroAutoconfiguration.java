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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hswebframework.web.AopUtils;
import org.hswebframework.web.authorization.Authorization;
import org.hswebframework.web.authorization.AuthorizationSupplier;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.access.FieldAccessController;
import org.hswebframework.web.authorization.shiro.boost.BoostAuthorizationAttributeSourceAdvisor;
import org.hswebframework.web.authorization.shiro.boost.DefaultDataAccessController;
import org.hswebframework.web.authorization.shiro.boost.DefaultFieldAccessController;
import org.hswebframework.web.authorization.shiro.boost.MethodInterceptorHolder;
import org.hswebframework.web.authorization.shiro.cache.SpringCacheManagerWrapper;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
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

import java.util.Collections;
import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
@EnableConfigurationProperties(ShiroProperties.class)
public class ShiroAutoconfiguration {

    @Autowired(required = false)
    private org.springframework.cache.CacheManager cacheManager;

    @Autowired
    private ShiroProperties shiroProperties;

    @Bean
    public CacheManager shiroCacheManager() {
        if (cacheManager == null) {
            return new MemoryConstrainedCacheManager();
        } else {
            return new SpringCacheManagerWrapper(cacheManager);
        }
    }

    @Bean
    public ListenerAuthorizingRealm listenerAuthorizingRealm(CacheManager cacheManager) {
        ListenerAuthorizingRealm realm = new ListenerAuthorizingRealm();
        realm.setCacheManager(cacheManager);
        return realm;
    }

    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor getLifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean(name = "securityManager")
    @ConditionalOnWebApplication
    public DefaultWebSecurityManager defaultWebSecurityManager(ListenerAuthorizingRealm authorizingRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(authorizingRealm);
        securityManager.setCacheManager(authorizingRealm.getCacheManager());
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    @Bean(name = "securityManager")
    @ConditionalOnNotWebApplication
    public DefaultSecurityManager defaultSecurityManager(ListenerAuthorizingRealm authorizingRealm) {
        DefaultSecurityManager securityManager = new DefaultSecurityManager();
        securityManager.setRealm(authorizingRealm);
        securityManager.setCacheManager(authorizingRealm.getCacheManager());
        securityManager.setSessionManager(new DefaultSessionManager());
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

//    @Bean
//    @ConditionalOnMissingBean
//    public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
//        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
//        advisorAutoProxyCreator.setProxyTargetClass(true);
//        return advisorAutoProxyCreator;
//    }

    @Autowired(required = false)
    private List<DataAccessHandler> dataAccessHandlers;

    @Bean
    @ConditionalOnMissingBean
    public DefaultDataAccessController defaultDataAccessController() {
        DefaultDataAccessController accessController = new DefaultDataAccessController();
        if (dataAccessHandlers != null) {
            dataAccessHandlers.forEach(accessController::addHandler);
        }
        return accessController;
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultFieldAccessController defaultFieldAccessController() {
        return new DefaultFieldAccessController();
    }

    @Bean
    public BoostAuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager,
                                                                                        DataAccessController dataAccessController,
                                                                                        FieldAccessController fieldAccessController) {
        BoostAuthorizationAttributeSourceAdvisor advisor = new BoostAuthorizationAttributeSourceAdvisor(dataAccessController, fieldAccessController);
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    @Bean
    public AuthorizationSupplier authorizationSupplier() {
        return () ->
                (Authorization) SecurityUtils.getSubject().getSession().getAttribute(Authorization.class.getName());
    }

    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 必须设置 SecurityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        if (null != shiroProperties)
            shiroFilterFactoryBean.setFilterChainDefinitionMap(shiroProperties.getFilters());
        else
            shiroFilterFactoryBean.setFilterChainDefinitionMap(Collections.emptyMap());
        return shiroFilterFactoryBean;
    }

    @Bean
    public MethodInterceptorHolderAdvisor methodInterceptorHolderAdvisor() {
        return new MethodInterceptorHolderAdvisor();
    }

    @Aspect
    @Order(Ordered.HIGHEST_PRECEDENCE)
    static class MethodInterceptorHolderAdvisor {
        @Around(value = "@annotation(org.hswebframework.web.authorization.annotation.RequiresExpression)"
                + "||@annotation(org.hswebframework.web.authorization.annotation.RequiresDataAccess)"
                + "||@annotation(org.hswebframework.web.authorization.annotation.Authorize)"
                + "||("
                + "within(@org.hswebframework.web.authorization.annotation.Authorize *) "
                + "&& ("
                + "@annotation(org.springframework.web.bind.annotation.RequestMapping)||"
                + "execution(org.hswebframework.web.controller.message.ResponseMessage *(..)"
                + ")))"
        )
        public Object around(ProceedingJoinPoint pjp) throws Throwable {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            String methodName = AopUtils.getMethodBody(pjp);
            String className = pjp.getTarget().getClass().getName();
            String id = DigestUtils.md5Hex(className.concat(methodName));
            new MethodInterceptorHolder(id, signature.getMethod(), pjp.getTarget(), AopUtils.getArgsMap(pjp))
                    .set();
            return pjp.proceed();
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
