package org.hswebframework.web.concurrent.lock.starter;

import org.hswebframework.web.concurrent.lock.LockManager;
import org.hswebframework.web.concurrent.lock.SimpleLockManager;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 */
@Configuration
public class LockManagerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(LockManager.class)
    public LockManager lockManager() {
        return new SimpleLockManager();
    }

    @Bean
    public AopLockAdvisor aopLockAdvisor(LockManager lockManager) {
        return new AopLockAdvisor(lockManager);
    }
}
