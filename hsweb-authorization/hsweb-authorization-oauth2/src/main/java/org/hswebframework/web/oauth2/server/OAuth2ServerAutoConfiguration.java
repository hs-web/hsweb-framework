package org.hswebframework.web.oauth2.server;

import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.oauth2.server.authentication.ClientSecretAuthenticationRequestConverter;
import org.hswebframework.web.oauth2.server.authentication.CompositeReactiveOAuth2ClientAuthenticationRequestResolver;
import org.hswebframework.web.oauth2.server.authentication.CompositeReactiveOAuth2ClientAuthenticator;
import org.hswebframework.web.oauth2.server.authentication.DefaultOAuth2ClientSecretAuthenticationProvider;
import org.hswebframework.web.oauth2.server.authentication.ReactiveOAuth2ClientAuthenticationProvider;
import org.hswebframework.web.oauth2.server.authentication.ReactiveOAuth2ClientAuthenticationRequestConverter;
import org.hswebframework.web.oauth2.server.authentication.ReactiveOAuth2ClientAuthenticationRequestResolver;
import org.hswebframework.web.oauth2.server.authentication.ReactiveOAuth2ClientAuthenticator;
import org.hswebframework.web.oauth2.server.code.AuthorizationCodeGranter;
import org.hswebframework.web.oauth2.server.code.DefaultAuthorizationCodeGranter;
import org.hswebframework.web.oauth2.server.credential.ClientCredentialGranter;
import org.hswebframework.web.oauth2.server.credential.ClientCredentialGrantHandler;
import org.hswebframework.web.oauth2.server.credential.CompositeClientCredentialGranter;
import org.hswebframework.web.oauth2.server.credential.DefaultClientCredentialGranter;
import org.hswebframework.web.oauth2.server.credential.DefaultClientCredentialGrantHandler;
import org.hswebframework.web.oauth2.server.impl.CompositeOAuth2GrantService;
import org.hswebframework.web.oauth2.server.impl.RedisAccessTokenManager;
import org.hswebframework.web.oauth2.server.refresh.DefaultRefreshTokenGranter;
import org.hswebframework.web.oauth2.server.refresh.RefreshTokenGranter;
import org.hswebframework.web.oauth2.server.web.OAuth2AuthorizeController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties(OAuth2Properties.class)
public class OAuth2ServerAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveOAuth2ServerAutoConfiguration {


        @Bean
        @ConditionalOnMissingBean
        public AccessTokenManager accessTokenManager(ReactiveRedisOperations<Object, Object> redis,
                                                     UserTokenManager tokenManager,
                                                     OAuth2Properties properties) {
            @SuppressWarnings("all")
            RedisAccessTokenManager manager = new RedisAccessTokenManager((ReactiveRedisOperations) redis, tokenManager);
            manager.setTokenExpireIn((int) properties.getTokenExpireIn().getSeconds());
            manager.setRefreshExpireIn((int) properties.getRefreshTokenIn().getSeconds());
            return manager;
        }

        @Bean
        @ConditionalOnMissingBean(ClientCredentialGranter.class)
        public ClientCredentialGranter clientCredentialGranter(
                AccessTokenManager accessTokenManager,
                ApplicationEventPublisher eventPublisher,
                ObjectProvider<ClientCredentialGrantHandler> handlerProvider) {
            ClientCredentialGranter legacyGranter =
                    new DefaultClientCredentialGranter(accessTokenManager, eventPublisher);

            List<ClientCredentialGrantHandler> handlers = new ArrayList<>();
            handlers.add(new DefaultClientCredentialGrantHandler(legacyGranter));
            handlerProvider.orderedStream().forEach(handlers::add);
            return new CompositeClientCredentialGranter(handlers);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(OAuth2ClientManager.class)
        public ReactiveOAuth2ClientAuthenticator reactiveOAuth2ClientAuthenticator(
                OAuth2ClientManager clientManager,
                ObjectProvider<ReactiveOAuth2ClientAuthenticationProvider> providerProvider) {
            List<ReactiveOAuth2ClientAuthenticationProvider> providers = new ArrayList<>();
            providerProvider.orderedStream().forEach(providers::add);
            return new CompositeReactiveOAuth2ClientAuthenticator(
                    providers,
                    new DefaultOAuth2ClientSecretAuthenticationProvider(clientManager));
        }

        @Bean
        @ConditionalOnMissingBean
        public ReactiveOAuth2ClientAuthenticationRequestResolver oauth2ClientAuthenticationRequestResolver(
                ObjectProvider<ReactiveOAuth2ClientAuthenticationRequestConverter> converterProvider) {
            List<ReactiveOAuth2ClientAuthenticationRequestConverter> converters = new ArrayList<>();
            converterProvider.orderedStream().forEach(converters::add);
            return new CompositeReactiveOAuth2ClientAuthenticationRequestResolver(
                    converters,
                    new ClientSecretAuthenticationRequestConverter());
        }

        @Bean
        @ConditionalOnMissingBean
        public AuthorizationCodeGranter authorizationCodeGranter(AccessTokenManager tokenManager,
                                                                 ApplicationEventPublisher eventPublisher,
                                                                 ReactiveRedisConnectionFactory redisConnectionFactory) {
            return new DefaultAuthorizationCodeGranter(tokenManager,eventPublisher, redisConnectionFactory);
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
        public OAuth2AuthorizeController oAuth2AuthorizeController(
                OAuth2GrantService grantService,
                OAuth2ClientManager clientManager,
                OAuth2Properties properties,
                ReactiveOAuth2ClientAuthenticator clientAuthenticator,
                ReactiveOAuth2ClientAuthenticationRequestResolver requestResolver) {
            return new OAuth2AuthorizeController(grantService,
                                                 clientManager,
                                                 properties,
                                                 clientAuthenticator,
                                                 requestResolver);
        }

    }

}
