package org.hswebframework.web.oauth2.server;

import org.hswebframework.web.authorization.ReactiveAuthenticationHolder;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.authorization.basic.web.ReactiveUserTokenParser;
import org.hswebframework.web.oauth2.server.auth.ReactiveOAuth2AccessTokenParser;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeGranter;
import org.hswebframework.web.oauth2.server.code.DefaultAuthorizationCodeGranter;
import org.hswebframework.web.oauth2.server.credential.ClientCredentialGranter;
import org.hswebframework.web.oauth2.server.credential.DefaultClientCredentialGranter;
import org.hswebframework.web.oauth2.server.impl.CompositeOAuth2GrantService;
import org.hswebframework.web.oauth2.server.impl.RedisAccessTokenManager;
import org.hswebframework.web.oauth2.server.refresh.DefaultRefreshTokenGranter;
import org.hswebframework.web.oauth2.server.refresh.RefreshTokenGranter;
import org.hswebframework.web.oauth2.server.web.OAuth2AuthorizeController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;

@Configuration(proxyBeanMethods = false)
public class OAuth2ServerAutoConfiguration {


    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ReactiveUserTokenParser.class)
    static class ReactiveOAuth2AccessTokenParserConfiguration {

        @Bean
        @ConditionalOnBean(AccessTokenManager.class)
        public ReactiveOAuth2AccessTokenParser reactiveOAuth2AccessTokenParser(AccessTokenManager accessTokenManager) {
            ReactiveOAuth2AccessTokenParser parser = new ReactiveOAuth2AccessTokenParser(accessTokenManager);
            ReactiveAuthenticationHolder.addSupplier(parser);
            return parser;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveOAuth2ServerAutoConfiguration {


        @Bean
        @ConditionalOnMissingBean
        public AccessTokenManager accessTokenManager(ReactiveRedisConnectionFactory redisConnectionFactory) {
            return new RedisAccessTokenManager(redisConnectionFactory);
        }

        @Bean
        @ConditionalOnMissingBean
        public ClientCredentialGranter clientCredentialGranter(ReactiveAuthenticationManager authenticationManager,
                                                               AccessTokenManager accessTokenManager) {
            return new DefaultClientCredentialGranter(authenticationManager, accessTokenManager);
        }

        @Bean
        @ConditionalOnMissingBean
        public AuthorizationCodeGranter authorizationCodeGranter(AccessTokenManager tokenManager,
                                                                 ReactiveRedisConnectionFactory redisConnectionFactory) {
            return new DefaultAuthorizationCodeGranter(tokenManager, redisConnectionFactory);
        }

        @Bean
        @ConditionalOnMissingBean
        public RefreshTokenGranter refreshTokenGranter(AccessTokenManager tokenManager) {
            return new DefaultRefreshTokenGranter(tokenManager);
        }

        @Bean
        @ConditionalOnMissingBean
        public OAuth2GrantService oAuth2GrantService(ObjectProvider<AuthorizationCodeGranter> codeProvider,
                                                     ObjectProvider<ClientCredentialGranter> credentialProvider,
                                                     ObjectProvider<RefreshTokenGranter> refreshProvider) {
            CompositeOAuth2GrantService grantService = new CompositeOAuth2GrantService();
            grantService.setAuthorizationCodeGranter(codeProvider.getIfAvailable());
            grantService.setClientCredentialGranter(credentialProvider.getIfAvailable());
            grantService.setRefreshTokenGranter(refreshProvider.getIfAvailable());

            return grantService;
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(OAuth2ClientManager.class)
        public OAuth2AuthorizeController oAuth2AuthorizeController(OAuth2GrantService grantService,
                                                                   OAuth2ClientManager clientManager) {
            return new OAuth2AuthorizeController(grantService, clientManager);
        }

    }

}
