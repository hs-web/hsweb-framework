package org.hswebframework.web.authorization.oauth2.client;

import org.hswebframework.expands.request.RequestBuilder;
import org.hswebframework.expands.request.SimpleRequestBuilder;
import org.hswebframework.web.authorization.builder.AuthenticationBuilderFactory;
import org.hswebframework.web.authorization.oauth2.client.request.DefaultResponseJudge;
import org.hswebframework.web.authorization.oauth2.client.simple.*;
import org.hswebframework.web.authorization.oauth2.client.simple.provider.HswebResponseConvertSupport;
import org.hswebframework.web.authorization.oauth2.client.simple.provider.HswebResponseJudgeSupport;
import org.hswebframework.web.authorization.oauth2.client.simple.request.builder.SimpleOAuth2RequestBuilderFactory;
import org.hswebframework.web.concurrent.lock.LockManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author zhouhao
 * @since 3.0
 */
public class OAuth2ClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RequestBuilder.class)
    public RequestBuilder requestBuilder() {
        return new SimpleRequestBuilder();
    }

    @Bean
    public HswebResponseJudgeSupport hswebResponseJudgeSupport() {
        return new HswebResponseJudgeSupport();
    }

    @Bean
    @ConditionalOnMissingBean(OAuth2RequestBuilderFactory.class)
    public SimpleOAuth2RequestBuilderFactory simpleOAuth2RequestBuilderFactory(RequestBuilder requestBuilder,
                                                                               AuthenticationBuilderFactory authenticationBuilderFactory) {
        SimpleOAuth2RequestBuilderFactory builderFactory = new SimpleOAuth2RequestBuilderFactory();
        builderFactory.setRequestBuilder(requestBuilder);
        builderFactory.setDefaultConvertHandler(new HswebResponseConvertSupport(authenticationBuilderFactory));
        builderFactory.setDefaultResponseJudge(new DefaultResponseJudge());
        return builderFactory;
    }

    @ConditionalOnMissingBean(OAuth2RequestService.class)
    @Bean
    public SimpleOAuth2RequestService simpleOAuth2RequestService(OAuth2ServerConfigRepository configRepository
            , OAuth2UserTokenRepository userTokenRepository
            , OAuth2RequestBuilderFactory builderFactory
            , LockManager lockManager) {

        return new SimpleOAuth2RequestService(configRepository, userTokenRepository, builderFactory,lockManager);
    }

    @ConditionalOnMissingBean(OAuth2ServerConfigRepository.class)
    @Bean
    @ConfigurationProperties(prefix = "hsweb.oauth2")
    public MemoryOAuth2ServerConfigRepository memoryOAuth2ServerConfigRepository() {
        return new MemoryOAuth2ServerConfigRepository();
    }

    @ConditionalOnMissingBean(OAuth2UserTokenRepository.class)
    @Bean
    public MemoryOAuth2UserTokenRepository memoryOAuth2UserTokenRepository() {
        return new MemoryOAuth2UserTokenRepository();
    }
}
