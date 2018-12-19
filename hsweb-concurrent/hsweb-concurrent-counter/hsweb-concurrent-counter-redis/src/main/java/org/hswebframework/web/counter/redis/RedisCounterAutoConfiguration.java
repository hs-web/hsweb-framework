package org.hswebframework.web.counter.redis;

import org.hswebframework.web.concurrent.counter.BloomFilterManager;
import org.hswebframework.web.concurrent.counter.CounterAutoConfiguration;
import org.hswebframework.web.concurrent.counter.CounterManager;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0
 */
@Configuration
@AutoConfigureBefore(CounterAutoConfiguration.class)
public class RedisCounterAutoConfiguration {

    @Bean
    @ConditionalOnBean(RedissonClient.class)
    public CounterManager counterManager(RedissonClient client) {
        return new RedissonCounterManager(client);
    }


    @Bean
    @ConditionalOnBean(BloomFilterManager.class)
    public BloomFilterManager bloomFilterManager(RedissonClient client) {
        return new RedisBloomFilterManager(client);
    }
}
