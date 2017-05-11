package org.hswebframework.web.concurrent.lock.starter;

import org.hswebframework.web.concurrent.lock.redis.RedissonLockManager;
import org.redisson.Redisson;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
@ConditionalOnClass(Redisson.class)
@ConditionalOnBean(Redisson.class)
public class RedisLockFactoryAutoConfiguration {
    @Bean
    public RedissonLockManager redissonLockFactory(Redisson redisson) {
        return new RedissonLockManager(redisson);
    }
}
