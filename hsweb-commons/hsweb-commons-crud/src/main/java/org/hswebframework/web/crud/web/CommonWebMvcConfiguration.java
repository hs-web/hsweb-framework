package org.hswebframework.web.crud.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice.class)
public class CommonWebMvcConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CommonWebMvcErrorControllerAdvice commonErrorControllerAdvice() {
        return new CommonWebMvcErrorControllerAdvice();
    }


    @SuppressWarnings("all")
    @Bean
    @ConditionalOnProperty(prefix = "hsweb.webflux.response-wrapper", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConfigurationProperties(prefix = "hsweb.webflux.response-wrapper")
    public ResponseMessageWrapperAdvice responseMessageWrapper() {
        return new ResponseMessageWrapperAdvice();
    }


}
