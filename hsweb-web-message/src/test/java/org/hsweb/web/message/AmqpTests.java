package org.hsweb.web.message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;
import javax.jms.Message;
import java.util.HashMap;


/**
 * @author zhouhao
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleActiveMQApplication.class, properties = "application.yml")
@EnableJms
public class AmqpTests {

    @Autowired
    private JmsTemplate template;

    @Test
    public void testSend() throws InterruptedException {

        for (int i = 0; i < 100; i++) {
            template.convertAndSend("test2", "aaaa" + i);
        }
        Thread.sleep(10000);

    }

}
