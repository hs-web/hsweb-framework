package org.hswebframework.web.concurrent.counter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0
 */
@Configuration
@ConditionalOnMissingBean(CounterManager.class)
public class CounterAutoConfiguration {

    @Bean
    public CounterManager inMemoryCounterManager() {
        return new SimpleCounterManager();
    }
}
