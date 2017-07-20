package org.hswebframework.web.message.jms;

import org.hswebframework.web.message.Messager;
import org.hswebframework.web.message.builder.StaticMessageBuilder;
import org.hswebframework.web.message.builder.StaticMessageSubjectBuilder;
import org.hswebframework.web.message.support.TextMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hswebframework.web.message.builder.StaticMessageBuilder.*;
import static org.hswebframework.web.message.builder.StaticMessageSubjectBuilder.queue;
import static org.hswebframework.web.message.builder.StaticMessageSubjectBuilder.topic;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleActiveMQApplication.class, properties = "application.yml")
@EnableJms
public class JmsMessagerTest {
    @Autowired
    private JmsTemplate jmsTemplate;

    private Messager messager;

    @Before
    public void setup() {
        messager = new JmsMessager(jmsTemplate);
    }

    static {
        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "*");
    }

    @Test
    public void testQueue() throws InterruptedException {

        messager.<TextMessage>subscribe(queue("test"))
                .onMessage(textMessage -> System.out.println(textMessage.getMessage() + " sub1"));
        messager.<TextMessage>subscribe(queue("test"))
                .onMessage(textMessage -> System.out.println(textMessage.getMessage() + " sub2"));


        for (int i = 0; i < 100; i++) {
            Thread.sleep(1000);
            messager.publish(text("hello jms"))
                    .to(queue("test"))
                    .send();
        }
    }

    @Test
    public void testTopic() throws InterruptedException {

        messager.<TextMessage>subscribe(topic("test"))
                .onMessage(textMessage -> System.out.println(textMessage.getMessage() + " topic1"));
        messager.<TextMessage>subscribe(topic("test"))
                .onMessage(textMessage -> System.out.println(textMessage.getMessage() + " topic2"));


        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            messager.publish(text("hello jms"))
                    .to(topic("test"))
                    .send();
        }
        
    }
}