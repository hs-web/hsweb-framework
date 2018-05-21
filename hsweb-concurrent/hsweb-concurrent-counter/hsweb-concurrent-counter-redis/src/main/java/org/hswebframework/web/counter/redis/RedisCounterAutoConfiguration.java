package org.hswebframework.web.counter.redis;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0
 */
@Configuration
@ConditionalOnMissingBean(RedissonCounterManager.class)
@ConditionalOnBean(RedissonClient.class)
public class RedisCounterAutoConfiguration {

    @Bean
    public RedissonCounterManager redissonCounterManager(RedissonClient client) {
        return new RedissonCounterManager(client);
    }
}
