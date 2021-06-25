package org.hswebframework.web.authorization.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class AuthorizationI18nConfiguration {

    @Bean
    public MessageSource authorizationMessageSource(){
        ResourceBundleMessageSource messageSource=new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBundleClassLoader(AuthorizationI18nConfiguration.class.getClassLoader());
        messageSource.setBasenames("i18n/authentication/messages");
        return messageSource;
    }

}
