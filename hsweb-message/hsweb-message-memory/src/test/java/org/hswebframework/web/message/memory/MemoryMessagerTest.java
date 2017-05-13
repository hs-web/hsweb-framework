package org.hswebframework.web.message.memory;

import org.hswebframework.web.message.MessageSubscribe;
import org.hswebframework.web.message.Messager;
import org.hswebframework.web.message.support.TextMessage;
import org.junit.Test;

import static org.hswebframework.web.message.builder.StaticMessageBuilder.text;
import static org.hswebframework.web.message.builder.StaticMessageSubjectBuilder.queue;
import static org.hswebframework.web.message.builder.StaticMessageSubjectBuilder.topic;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class MemoryMessagerTest {

    private Messager messager = new MemoryMessager();

    @Test
    public void testQueue() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            int x = i;
            MessageSubscribe<TextMessage> sub = messager.subscribe(queue("test"));
            sub.onMessage(msg -> System.out.println(x + msg.getMessage()));
        }
        for (int i = 0; i < 10; i++) {
            Thread.sleep(200);
            messager.publish(text("hello queue" + i))
                    .to(queue("test")).send();
        }

        Thread.sleep(1000);
    }

    @Test
    public void testTopic() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            int x = i;
            MessageSubscribe<TextMessage> sub = messager.subscribe(topic("test"));
            sub.onMessage(msg -> System.out.println(x + msg.getMessage()));
        }
        for (int i = 0; i < 10; i++) {
            Thread.sleep(200);
            messager.publish(text("hello queue" + i))
                    .to(topic("test")).send();
        }

        Thread.sleep(1000);
    }
}