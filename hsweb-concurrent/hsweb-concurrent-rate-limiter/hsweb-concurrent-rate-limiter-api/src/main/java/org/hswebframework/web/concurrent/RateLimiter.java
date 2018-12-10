package org.hswebframework.web.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 3.0.4
 */
public interface RateLimiter {

    boolean tryAcquire(int permits, long timeout, TimeUnit timeUnit);

    default boolean tryAcquire(long timeout, TimeUnit unit) {
        return tryAcquire(1, timeout, unit);
    }

}
