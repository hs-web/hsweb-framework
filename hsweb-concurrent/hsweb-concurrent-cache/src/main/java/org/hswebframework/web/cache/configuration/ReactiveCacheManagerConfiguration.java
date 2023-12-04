package org.hswebframework.web.cache.configuration;

import org.hswebframework.web.cache.ReactiveCacheManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnMissingBean(ReactiveCacheManager.class)
@EnableConfigurationProperties(ReactiveCacheProperties.class)
public class ReactiveCacheManagerConfiguration {


    @Bean
    public ReactiveCacheManager reactiveCacheManager(ReactiveCacheProperties properties, ApplicationContext context) {

        return properties.createCacheManager(context);

    }


}
