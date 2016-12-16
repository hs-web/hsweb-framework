package org.hsweb.web.message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;


/**
 * @author zhouhao
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleActiveMQApplication.class, properties = "application.yml")
@EnableJms
public class AmqpTests {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Test
    public void testSend() {
        jmsMessagingTemplate.convertAndSend("test", new HashMap<>());
    }

}
