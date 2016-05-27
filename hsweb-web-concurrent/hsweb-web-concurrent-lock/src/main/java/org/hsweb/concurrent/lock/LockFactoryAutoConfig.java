package org.hsweb.concurrent.lock;

import org.hsweb.concurrent.lock.support.AnnotationLockAopAdvice;
import org.hsweb.concurrent.lock.support.DefaultLockFactory;
import org.hsweb.concurrent.lock.support.redis.RedisLockFactoryAutoConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by zhouhao on 16-4-27.
 */
@Configuration
@ImportAutoConfiguration(RedisLockFactoryAutoConfig.class)
public class LockFactoryAutoConfig {

    @Bean
    @ConditionalOnMissingBean(LockFactory.class)
    public LockFactory defaultLockFactory() {
        return new DefaultLockFactory();
    }

    @Bean
    public AnnotationLockAopAdvice annotationLockAopAdvice() {
        return new AnnotationLockAopAdvice();
    }

}
