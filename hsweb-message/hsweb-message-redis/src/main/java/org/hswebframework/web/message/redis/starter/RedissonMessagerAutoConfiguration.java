package org.hswebframework.web.message.redis.starter;

import org.hswebframework.web.message.Messager;
import org.hswebframework.web.message.redis.RedissonMessager;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 */
@Configuration
@ConditionalOnBean(RedissonClient.class)
@ConditionalOnMissingBean(Messager.class)
public class RedissonMessagerAutoConfiguration {
    @Bean
    public Messager messager(RedissonClient client) {
        return new RedissonMessager(client);
    }
}
