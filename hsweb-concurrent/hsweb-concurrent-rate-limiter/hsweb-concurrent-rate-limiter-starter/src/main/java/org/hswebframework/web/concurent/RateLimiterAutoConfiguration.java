package org.hswebframework.web.concurent;

import org.hswebframework.web.concurrent.GuavaRateLimiterManager;
import org.hswebframework.web.concurrent.RateLimiterManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0.4
 */
@Configuration
public class RateLimiterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RateLimiterManager.class)
    @ConditionalOnClass(name = "com.google.common.util.concurrent.RateLimiter")
    public GuavaRateLimiterManager guavaRateLimiterManager() {
        return new GuavaRateLimiterManager();
    }

    @Bean
    @ConditionalOnBean(RateLimiterManager.class)
    public RateLimiterAopAdvisor rateLimiterAopAdvisor(RateLimiterManager rateLimiterManager) {
        return new RateLimiterAopAdvisor(rateLimiterManager);
    }
}
