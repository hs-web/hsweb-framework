package org.hswebframework.web.concurent;

import org.hswebframework.web.concurrent.annotation.RateLimiter;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhouhao
 * @since 3.0.4
 */
@Service
public class TestService {

    public static AtomicLong counter = new AtomicLong();


    @RateLimiter(permits = 1, acquire = 500, acquireTimeUnit = TimeUnit.MILLISECONDS) //一秒一次
    public String test() {
        counter.incrementAndGet();
        return "ok";
    }

    @RateLimiter(key = "${#name}", permits = 1, acquire = 500, acquireTimeUnit = TimeUnit.MILLISECONDS)
    public String test(String name) {
        counter.incrementAndGet();
        return name;
    }
}
