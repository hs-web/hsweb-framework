package org.hswebframework.web.concurrent.lock.starter;

import org.hswebframework.web.concurrent.lock.LockManager;
import org.hswebframework.web.concurrent.lock.redis.RedissonLockManager;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@SpringBootApplication
@EnableScheduling
public class TestApplication {
//    @Bean
//    public RedissonClient redissonClient() {
//        Config config = new Config();
//        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
//        config.setLockWatchdogTimeout(60_1000);
//
//        return Redisson.create();
//    }

//    @Bean
//    public LockManager lockManager(RedissonClient redissonClient) {
//        return new RedissonLockManager(redissonClient);
//    }

    @Bean
    public LockService lockService() {
        return new LockService();
    }
}