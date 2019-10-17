package org.hswebframework.web.authorization.basic.configuration;

import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.authorization.ReactiveAuthenticationManagerProvider;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.basic.aop.AopMethodAuthorizeDefinitionParser;
import org.hswebframework.web.authorization.basic.embed.EmbedAuthenticationProperties;
import org.hswebframework.web.authorization.basic.embed.EmbedReactiveAuthenticationManager;
import org.hswebframework.web.authorization.basic.handler.DefaultAuthorizingHandler;
import org.hswebframework.web.authorization.basic.handler.UserAllowPermissionHandler;
import org.hswebframework.web.authorization.basic.handler.access.DefaultDataAccessController;
import org.hswebframework.web.authorization.basic.twofactor.TwoFactorHandlerInterceptorAdapter;
import org.hswebframework.web.authorization.basic.web.*;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.authorization.twofactor.TwoFactorValidatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * 权限控制自动配置类
 *
 * @author zhouhao
 * @since 3.0
 */
@Configuration
@EnableConfigurationProperties(EmbedAuthenticationProperties.class)
public class AuthorizingHandlerAutoConfiguration {

    @Bean
    public DefaultDataAccessController dataAccessController() {
        return new DefaultDataAccessController();
    }

    @Bean
    public DefaultAuthorizingHandler authorizingHandler(DataAccessController dataAccessController) {
        return new DefaultAuthorizingHandler(dataAccessController);
    }

    @Bean
    @ConditionalOnMissingBean(UserTokenParser.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public UserTokenParser userTokenParser() {
        return new SessionIdUserTokenParser();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public SessionIdUserTokenGenerator sessionIdUserTokenGenerator() {
        return new SessionIdUserTokenGenerator();
    }

    @Bean
    @ConditionalOnProperty(prefix = "hsweb.authorize.two-factor", name = "enable", havingValue = "true")
    @Order(100)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public WebMvcConfigurer twoFactorHandlerConfigurer(TwoFactorValidatorManager manager) {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new TwoFactorHandlerInterceptorAdapter(manager));
                super.addInterceptors(registry);
            }
        };
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public UserTokenWebFilter userTokenWebFilter(){
        return new UserTokenWebFilter();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public WebMvcConfigurer webUserTokenInterceptorConfigurer(UserTokenManager userTokenManager,
                                                              AopMethodAuthorizeDefinitionParser parser,
                                                              List<UserTokenParser> userTokenParser) {

        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new WebUserTokenInterceptor(userTokenManager, userTokenParser, parser));

            }
        };
    }

    @Bean
//    @ConditionalOnMissingBean(ReactiveAuthenticationManager.class)
    public ReactiveAuthenticationManagerProvider embedAuthenticationManager(EmbedAuthenticationProperties properties) {
        return new EmbedReactiveAuthenticationManager(properties);
    }

    @Bean
    public UserAllowPermissionHandler userAllowPermissionHandler() {
        return new UserAllowPermissionHandler();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public UserOnSignIn userOnSignIn(UserTokenManager userTokenManager) {
        return new UserOnSignIn(userTokenManager);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public UserOnSignOut userOnSignOut(UserTokenManager userTokenManager) {
        return new UserOnSignOut(userTokenManager);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConfigurationProperties(prefix = "hsweb.authorize.token.default")
    public DefaultUserTokenGenPar defaultUserTokenGenPar(){
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

    @Configuration
    public static class DataAccessHandlerProcessor implements BeanPostProcessor {

        @Autowired
        private DefaultDataAccessController defaultDataAccessController;

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            if (bean instanceof DataAccessHandler) {
                defaultDataAccessController.addHandler(((DataAccessHandler) bean));
            }
            return bean;
        }
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
}
