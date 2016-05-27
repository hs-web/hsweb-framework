package org.hsweb.concurrent.lock.support.redis;

import org.hsweb.concurrent.lock.LockFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * Created by zhouhao on 16-5-27.
 */
@Configuration
@ConditionalOnBean(RedisTemplate.class)
@ConditionalOnMissingBean(LockFactory.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RedisLockFactoryAutoConfig {

    @Resource
    public RedisTemplate redisTemplate;

    @Bean
    public RedisLockFactory redisLockFactory() {
        RedisLockFactory lockFactory = new RedisLockFactory();
        lockFactory.setRedisTemplate(redisTemplate);
        return lockFactory;
    }
}
