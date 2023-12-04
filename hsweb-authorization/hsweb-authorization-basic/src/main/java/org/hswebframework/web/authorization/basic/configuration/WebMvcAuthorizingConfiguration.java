package org.hswebframework.web.authorization.basic.configuration;

import org.hswebframework.web.authorization.basic.aop.AopMethodAuthorizeDefinitionParser;
import org.hswebframework.web.authorization.basic.twofactor.TwoFactorHandlerInterceptorAdapter;
import org.hswebframework.web.authorization.basic.web.*;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.authorization.twofactor.TwoFactorValidatorManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Nonnull;
import java.util.List;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebMvcAuthorizingConfiguration {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnBean(AopMethodAuthorizeDefinitionParser.class)
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
    public UserOnSignIn userOnSignIn(UserTokenManager userTokenManager) {
        return new UserOnSignIn(userTokenManager);
    }

    @Bean
    public UserOnSignOut userOnSignOut(UserTokenManager userTokenManager) {
        return new UserOnSignOut(userTokenManager);
    }

    @SuppressWarnings("all")
    @ConfigurationProperties(prefix = "hsweb.authorize.token.default")
    public ServletUserTokenGenPar servletUserTokenGenPar() {
        return new ServletUserTokenGenPar();
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
    @ConditionalOnProperty(prefix = "hsweb.authorize.two-factor", name = "enable", havingValue = "true")
    @Order(100)
    public WebMvcConfigurer twoFactorHandlerConfigurer(TwoFactorValidatorManager manager) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(@Nonnull InterceptorRegistry registry) {
                registry.addInterceptor(new TwoFactorHandlerInterceptorAdapter(manager));
            }
        };
    }

}