package org.hswebframework.web.starter.i18n;

import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.i18n.MessageSourceInitializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;

import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class I18nConfiguration {


    @Bean
    public MessageSource coreMessageSource(){
        ResourceBundleMessageSource messageSource=new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBundleClassLoader(BusinessException.class.getClassLoader());
        messageSource.setBasenames("i18n/core/messages");
        return messageSource;
    }

    @Bean
    @Primary
    public MessageSource compositeMessageSource(ObjectProvider<MessageSource> objectProvider) {
        CompositeMessageSource messageSource = new CompositeMessageSource();
        messageSource.addMessageSources(objectProvider.stream().collect(Collectors.toList()));
        MessageSourceInitializer.init(messageSource);
        return messageSource;
    }


}
