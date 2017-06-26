package org.hswebframework.web.message.redis;

import org.hswebframework.web.message.Messager;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import static org.hswebframework.web.message.builder.StaticMessageSubjectBuilder.topic;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class RedissonMessagerTest {


    public void testSimple() {

    }

    public static void main(String[] args) throws InterruptedException {
        Config config = new Config();
        config.useSingleServer().setAddress("127.0.0.1:6379");
        RedissonClient redisson = Redisson.create(config);
        Messager messager = new RedissonMessager(redisson);

        byte[] stat = new byte[1];

//        new Thread(() -> {
//            for (int i = 0; i < 1000; i++) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                messager.publish(text("hello2"))
//                        .to(topic("test"))
//                        .from(user("admin"))
//                        .send();
//            }
//        }).start();
        messager.subscribe(topic("test"))
                .onMessage(System.out::println)
                .onMessage(msg -> stat[0] = 1);
        //redisson.shutdown();
    }
}