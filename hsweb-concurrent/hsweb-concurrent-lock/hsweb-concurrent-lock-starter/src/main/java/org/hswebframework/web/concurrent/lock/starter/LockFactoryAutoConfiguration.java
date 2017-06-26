package org.hswebframework.web.concurrent.lock.starter;

import org.hswebframework.web.concurrent.lock.LockManager;
import org.hswebframework.web.concurrent.lock.SimpleLockManager;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
@ImportAutoConfiguration(RedisLockFactoryAutoConfiguration.class)
public class LockFactoryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(LockManager.class)
    public SimpleLockManager simpleLockFactory() {
        return new SimpleLockManager();
    }
}
