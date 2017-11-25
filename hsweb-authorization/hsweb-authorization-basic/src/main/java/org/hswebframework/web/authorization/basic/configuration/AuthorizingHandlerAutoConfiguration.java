package org.hswebframework.web.authorization.basic.configuration;

import org.hswebframework.web.authorization.AuthenticationHolder;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.AuthenticationSupplier;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.basic.handler.DefaultAuthorizingHandler;
import org.hswebframework.web.authorization.basic.handler.access.DefaultDataAccessController;
import org.hswebframework.web.authorization.basic.web.*;
import org.hswebframework.web.authorization.basic.web.session.UserTokenAutoExpiredListener;
import org.hswebframework.web.authorization.token.DefaultUserTokenManager;
import org.hswebframework.web.authorization.token.UserTokenAuthenticationSupplier;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public UserTokenParser userTokenParser() {
        return new SessionIdUserTokenParser();
    }

    @Bean
    public SessionIdUserTokenGenerator sessionIdUserTokenGenerator() {
        return new SessionIdUserTokenGenerator();
    }


    @Bean
    public WebMvcConfigurer webUserTokenInterceptorConfigurer(UserTokenManager userTokenManager,
                                                              List<UserTokenParser> userTokenParser) {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new WebUserTokenInterceptor(userTokenManager, userTokenParser));
                super.addInterceptors(registry);
            }
        };
    }

    @Bean
    public UserOnSignIn userOnSignIn(UserTokenManager userTokenManager) {
        return new UserOnSignIn(userTokenManager);
    }

    @Bean
    public UserOnSignOut userOnSignOut(UserTokenManager userTokenManager) {
        return new UserOnSignOut(userTokenManager);
    }

    @Bean
    public UserTokenAutoExpiredListener userTokenAutoExpiredListener(UserTokenManager userTokenManager) {
        return new UserTokenAutoExpiredListener(userTokenManager);
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
            if (bean instanceof AuthenticationSupplier) {
                AuthenticationHolder.addSupplier(((AuthenticationSupplier) bean));
            }
            return bean;
        }
    }
}
