package org.hswebframework.web.socket;

import org.hswebframework.web.concurrent.counter.Counter;
import org.hswebframework.web.concurrent.counter.CounterManager;
import org.hswebframework.web.counter.redis.RedissonCounterManager;
import org.hswebframework.web.message.Messager;
import org.hswebframework.web.message.jms.JmsMessager;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
@EnableAutoConfiguration
@EnableJms
public class WebSocketServerTests {

    static {
        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "*");
    }

    @Bean
    public Messager messager(JmsTemplate template) {
        return new JmsMessager(template);
    }

    @Bean
    public TestProcessor testProcessor() {
        return new TestProcessor();
    }

    @Bean
    public CounterManager counterManager() {
        Config config = new Config();
        config.useSingleServer().setAddress("127.0.0.1:6379");
        RedissonClient client = Redisson.create(config);
        return new RedissonCounterManager(client);
    }

//    // 使用redis
//    @Bean(destroyMethod = "shutdown")
//    public RedissonClient redissonClient() {
//        Config config = new Config();
//        config.useSingleServer().setAddress("127.0.0.1:6379");
//        return Redisson.create(config);
//    }

    public static void main(String[] args) {
        SpringApplication.run(WebSocketServerTests.class);
    }
}