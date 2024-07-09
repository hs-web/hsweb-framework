package org.hswebframework.web.authorization.basic.configuration;

import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.ReactiveAuthenticationManagerProvider;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.basic.embed.EmbedAuthenticationProperties;
import org.hswebframework.web.authorization.basic.embed.EmbedReactiveAuthenticationManager;
import org.hswebframework.web.authorization.basic.handler.AuthorizationLoginLoggerInfoHandler;
import org.hswebframework.web.authorization.basic.handler.DefaultAuthorizingHandler;
import org.hswebframework.web.authorization.basic.handler.UserAllowPermissionHandler;
import org.hswebframework.web.authorization.basic.web.*;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * 权限控制自动配置类
 *
 * @author zhouhao
 * @since 3.0
 */
@AutoConfiguration
@EnableConfigurationProperties(EmbedAuthenticationProperties.class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class AuthorizingHandlerAutoConfiguration {


    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public DefaultAuthorizingHandler authorizingHandler() {
        return new DefaultAuthorizingHandler(null);
    }


    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public UserTokenWebFilter userTokenWebFilter(UserTokenManager userTokenManager,
                                                 ObjectProvider<ReactiveUserTokenParser> tokenParsers,
                                                 ObjectProvider<ReactiveUserTokenGenerator> tokenGenerators) {
        UserTokenWebFilter filter = new UserTokenWebFilter(userTokenManager);
        tokenParsers.forEach(filter::register);
        tokenGenerators.forEach(filter::register);

        return filter;
    }


    @Bean
    public ReactiveAuthenticationManagerProvider embedAuthenticationManager(EmbedAuthenticationProperties properties) {
        return new EmbedReactiveAuthenticationManager(properties);
    }

    @Bean
    public UserAllowPermissionHandler userAllowPermissionHandler() {
        return new UserAllowPermissionHandler();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConfigurationProperties(prefix = "hsweb.authorize.token.default")
    public DefaultUserTokenGenPar defaultUserTokenGenPar() {
        return new DefaultUserTokenGenPar();
    }

    @Bean
    public AuthorizationController authorizationController() {
        return new AuthorizationController();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public ReactiveUserTokenController userTokenController() {
        return new ReactiveUserTokenController();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public BearerTokenParser bearerTokenParser() {
        return new BearerTokenParser();
    }

    @Configuration
    @ConditionalOnProperty(prefix = "hsweb.authorize", name = "basic-authorization", havingValue = "true")
    @ConditionalOnClass(UserTokenForTypeParser.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class BasicAuthorizationConfiguration {
        @Bean
        public BasicAuthorizationTokenParser basicAuthorizationTokenParser(AuthenticationManager authenticationManager,
                                                                           UserTokenManager tokenManager) {
            return new BasicAuthorizationTokenParser(authenticationManager, tokenManager);
        }

    }


    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public AuthorizationLoginLoggerInfoHandler authorizationLoginLoggerInfoHandler() {
        return new AuthorizationLoginLoggerInfoHandler();
    }
}
