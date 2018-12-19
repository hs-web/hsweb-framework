package org.hswebframework.web.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 3.0.4
 */
public abstract class AbstractRateLimiterManager implements RateLimiterManager {
    private final Map<String, RateLimiter> counterStore = new HashMap<>(128);

    protected abstract RateLimiter createRateLimiter(String key, double permits, TimeUnit timeUnit);

    @Override
    public RateLimiter getRateLimiter(String key, double permits, TimeUnit timeUnit) {
        RateLimiter counter = counterStore.get(key);
        if (counter != null) {
            return counter;
        }
        synchronized (counterStore) {
            return counterStore.computeIfAbsent(key, k -> createRateLimiter(key, permits, timeUnit));
        }
    }
}
