package org.hswebframework.web.system.authorization.defaults.configuration;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.authorization.ReactiveAuthenticationInitializeService;
import org.hswebframework.web.authorization.ReactiveAuthenticationManagerProvider;
import org.hswebframework.web.authorization.define.AuthorizeDefinitionCustomizer;
import org.hswebframework.web.authorization.define.CompositeAuthorizeDefinitionCustomizer;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.hswebframework.web.system.authorization.api.service.reactive.ReactiveUserService;
import org.hswebframework.web.system.authorization.defaults.service.*;
import org.hswebframework.web.system.authorization.defaults.service.terms.DimensionTerm;
import org.hswebframework.web.system.authorization.defaults.service.terms.UserDimensionTerm;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


@EnableConfigurationProperties({AuthenticationInitializeProperties.class})
@AutoConfiguration
public class AuthorizationServiceAutoConfiguration {

    @Bean
    public SmartInitializingSingleton authenticationInitializeCustomizerExecutor(AuthenticationInitializeProperties properties,
                                                                                 ObjectProvider<AuthenticationInitializeCustomizer> customizers) {
        return () -> {
            for (AuthenticationInitializeCustomizer customizer : customizers) {
                customizer.customize(properties);
            }
        };
    }

    @AutoConfiguration
    public static class ReactiveAuthorizationServiceAutoConfiguration {

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
        public PermissionSynchronization permissionSynchronization(ReactiveRepository<PermissionEntity, String> permissionRepository,
                                                                   ObjectProvider<AuthorizeDefinitionCustomizer> customizer) {
            return new PermissionSynchronization(permissionRepository, new CompositeAuthorizeDefinitionCustomizer(customizer));
        }

        @Bean
        @ConditionalOnProperty(prefix = "hsweb.authorization.dynamic-dimension", name = "enabled", havingValue = "true", matchIfMissing = true)
        public DefaultDimensionService defaultDimensionService() {
            return new DefaultDimensionService();
        }

//        @Bean
//        public UserDimensionProvider userPermissionDimensionProvider() {
//            return new UserDimensionProvider();
//        }

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

        @Bean
        @ConditionalOnBean(UserTokenManager.class)
        public RemoveUserTokenWhenUserDisabled removeUserTokenWhenUserDisabled(UserTokenManager tokenManager) {
            return new RemoveUserTokenWhenUserDisabled(tokenManager);
        }
    }

    @Bean
    public UserDimensionTerm userDimensionTerm() {
        return new UserDimensionTerm();
    }

    @Bean
    public DimensionTerm dimensionTerm() {
        return new DimensionTerm();
    }

    @Bean
    public PermissionProperties permissionProperties() {
        return new PermissionProperties();
    }
}
