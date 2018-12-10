package org.hswebframework.web.concurrent;

import lombok.SneakyThrows;
import org.junit.Test;

import java.util.concurrent.TimeUnit;


/**
 * @author zhouhao
 * @since 3.0.4
 */
public class GuavaRateLimiterManagerTest {
    GuavaRateLimiterManager manager = new GuavaRateLimiterManager();

    @SneakyThrows
    @Test
    public void testRateLimiter() {
        RateLimiter limiter = manager.getRateLimiter("test", 1, TimeUnit.SECONDS);
        for (int i = 0; i < 100; i++) {
            if (!limiter.tryAcquire(10, TimeUnit.SECONDS)) {
                throw new UnsupportedOperationException();
            }
            System.out.println(i + ":" + System.currentTimeMillis());
        }

    }
}