package org.hswebframework.web.crud.web;

import org.hswebframework.web.i18n.WebFluxLocaleFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class CommonWebFluxConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CommonErrorControllerAdvice commonErrorControllerAdvice(MessageSource messageSource) {
        return new CommonErrorControllerAdvice(messageSource);
    }


    @Bean
    @ConditionalOnProperty(prefix = "hsweb.webflux.response-wrapper", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConfigurationProperties(prefix = "hsweb.webflux.response-wrapper")
    public ResponseMessageWrapper responseMessageWrapper(ServerCodecConfigurer codecConfigurer,
                                                         RequestedContentTypeResolver resolver,
                                                         ReactiveAdapterRegistry registry) {
        return new ResponseMessageWrapper(codecConfigurer.getWriters(), resolver, registry);
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public WebFilter localeWebFilter() {
        return new WebFluxLocaleFilter();
    }

}
