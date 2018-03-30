package org.hswebframework.web.eventbus.spring;

import org.hswebframework.web.eventbus.EventBus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author zhouhao
 * @since 3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class,webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SpringEventBusTest {

    @Autowired
    EventBus eventBus;

    @Test
    public void test() throws InterruptedException {
        System.out.println(Thread.currentThread().getName());
        long t = System.currentTimeMillis();
        eventBus.publish(eventBus);
        System.out.println(System.currentTimeMillis() - t);
        Thread.sleep(1000);
        Assert.assertEquals(Test2.counter.get(), 3);
    }


}
