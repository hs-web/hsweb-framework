package org.hswebframework.web.message.jms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringRunner;


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
        new Thread(() -> {
            while (true) {
                try {
                    Object obj = template.receiveAndConvert("test4");
                    System.out.println("----" + obj);
                } catch (Exception e) {
                    break;
                }
            }
        }).start();
        new Thread(() -> {
            while (true) {
                try {
                    Object obj = template.receiveAndConvert("test4");
                    System.out.println("----222" + obj);
                } catch (Exception e) {
                    break;
                }
            }
        }).start();
        Thread.sleep(100);
        int i = 0;
        while (i < 10) {
            template.convertAndSend("test4", "aaa" + i++);
            Thread.sleep(1000);
        }
        Thread.sleep(1000);

    }

}
