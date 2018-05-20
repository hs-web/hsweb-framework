package org.hswebframework.web.socket;

import org.hswebframework.web.concurrent.counter.CounterManager;
import org.hswebframework.web.concurrent.counter.SimpleCounterManager;
import org.hswebframework.web.message.Messager;
import org.hswebframework.web.message.memory.MemoryMessager;
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
//@EnableJms
public class WebSocketServerTests {

    static {
       // System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "*");
    }
//

//    @Bean(destroyMethod = "shutdown")
//    public RedissonClient redissonClient(){
//        Config config = new Config();
//        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
//        return Redisson.create(config);
//    }

    @Bean
    public CounterManager counterManager() {
        return new SimpleCounterManager();
    }

    @Bean
    public TestProcessor testProcessor() {
        return new TestProcessor();
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