package org.hswebframework.web.concurrent.counter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0
 */
@Configuration
public class CounterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CounterManager.class)
    public CounterManager counterManager() {
        return new SimpleCounterManager();
    }


    @Bean
    @ConditionalOnMissingBean(BloomFilterManager.class)
    @ConditionalOnClass(name = "com.google.common.hash.BloomFilter")
    public BloomFilterManager bloomFilterManager() {
        return new GuavaBloomFilterManager();
    }
}
