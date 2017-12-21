package org.hswebframework.web.concurrent.lock.starter;

import org.hswebframework.web.concurrent.lock.LockManager;
import org.hswebframework.web.concurrent.lock.redis.RedissonLockManager;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 */
@Configuration
@ConditionalOnClass({RedissonClient.class, RedissonLockManager.class})
@ConditionalOnBean(RedissonClient.class)
public class RedisLockManagerAutoConfiguration {
    @Bean
    public LockManager lockManager(RedissonClient redissonClient) {
        return new RedissonLockManager(redissonClient);
    }
}
