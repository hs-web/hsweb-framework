package org.hswebframework.web.system.authorization.defaults.configuration;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.authorization.ReactiveAuthenticationInitializeService;
import org.hswebframework.web.authorization.ReactiveAuthenticationManagerProvider;
import org.hswebframework.web.authorization.simple.DefaultAuthorizationAutoConfiguration;
import org.hswebframework.web.system.authorization.api.UserDimensionProvider;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.hswebframework.web.system.authorization.defaults.service.*;
import org.hswebframework.web.system.authorization.defaults.service.terms.UserDimensionTerm;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AuthorizationServiceAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @AutoConfigureBefore(DefaultAuthorizationAutoConfiguration.class)
    static class ReactiveAuthorizationServiceAutoConfiguration {
        @ConditionalOnBean(ReactiveRepository.class)
        @Bean
        public ReactiveUserService reactiveUserService() {
            return new DefaultReactiveUserService();
        }

        @Bean
        @ConditionalOnBean(ReactiveUserService.class)
        public ReactiveAuthenticationManagerProvider defaultReactiveAuthenticationManager() {
            return new DefaultReactiveAuthenticationManager();
        }

        @Bean
        @ConditionalOnBean(ReactiveUserService.class)
        public ReactiveAuthenticationInitializeService reactiveAuthenticationInitializeService() {
            return new DefaultReactiveAuthenticationInitializeService();
        }

        @Bean
        public PermissionSynchronization permissionSynchronization() {
            return new PermissionSynchronization();
        }

        @Bean
        public DefaultDimensionService defaultDimensionService() {
            return new DefaultDimensionService();
        }

        @Bean
        public UserDimensionProvider userPermissionDimensionProvider() {
            return new UserDimensionProvider();
        }

        @Bean
        public DefaultDimensionUserService defaultDimensionUserService() {
            return new DefaultDimensionUserService();
        }

        @Bean
        public DefaultAuthorizationSettingService defaultAuthorizationSettingService() {
            return new DefaultAuthorizationSettingService();
        }

        @Bean
        public DefaultPermissionService defaultPermissionService() {
            return new DefaultPermissionService();
        }

    }

    @Bean
    public UserDimensionTerm userDimensionTerm() {
        return new UserDimensionTerm();
    }

}
