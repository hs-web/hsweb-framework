package org.hswebframework.web.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 3.0.4
 */
public interface RateLimiterManager {

    RateLimiter getRateLimiter(String key, double permits, TimeUnit timeUnit);
}
