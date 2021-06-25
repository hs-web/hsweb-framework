package org.hswebframework.web.crud.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class Commons18nConfiguration {

    @Bean
    public MessageSource commonsMessageSource(){
        ResourceBundleMessageSource messageSource=new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBundleClassLoader(Commons18nConfiguration.class.getClassLoader());
        messageSource.setBasenames("i18n/commons/messages");
        return messageSource;
    }

}
