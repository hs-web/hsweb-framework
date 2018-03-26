package org.hswebframework.web.eventbus.spring;

import org.hswebframework.web.eventbus.EventBus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringEventBusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EventBus.class)
    public SpringEventBus springEventBus() {
        return new SpringEventBus();
    }
}
