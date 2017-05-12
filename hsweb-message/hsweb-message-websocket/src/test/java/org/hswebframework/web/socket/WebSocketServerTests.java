package org.hswebframework.web.socket;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
@EnableAutoConfiguration
public class WebSocketServerTests {

    @Bean
    public TestProcessor testProcessor() {
        return new TestProcessor();
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("127.0.0.1:6379");
        return Redisson.create(config);
    }

    public static void main(String[] args) {
        SpringApplication.run(WebSocketServerTests.class);
    }
}