package org.hsweb.concurrent.lock;

import org.hsweb.concurrent.lock.support.DefaultReadWriteLockFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by zhouhao on 16-4-27.
 */
@Configuration
@ConditionalOnMissingBean(value = {LockFactory.class})
public class ReadWriteLockFactoryAutoConfig {

    @Bean
    public LockFactory createReadWriteLockFactory() {
        return new DefaultReadWriteLockFactory();
    }
}
