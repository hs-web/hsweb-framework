package org.hswebframework.web.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 3.0.4
 */
public class GuavaRateLimiterManager extends AbstractRateLimiterManager {
    @Override
    protected RateLimiter createRateLimiter(String key, double permits, TimeUnit timeUnit) {
        long seconds = timeUnit.toSeconds(1);
        double permitsPerSecond = permits;

        if (seconds > 0) {
            permitsPerSecond = permits / timeUnit.toSeconds(1);
        } else {
            if (timeUnit == TimeUnit.MILLISECONDS) {
                permitsPerSecond = permits / 1000D;
            }
        }

        com.google.common.util.concurrent.RateLimiter rateLimiter =
                com.google.common.util.concurrent.RateLimiter.create(permitsPerSecond);
        return rateLimiter::tryAcquire;
    }
}
