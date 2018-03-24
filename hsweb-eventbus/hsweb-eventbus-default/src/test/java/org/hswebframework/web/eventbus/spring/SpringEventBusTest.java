package org.hswebframework.web.eventbus.spring;

import org.hswebframework.web.eventbus.annotation.EventMode;
import org.hswebframework.web.eventbus.annotation.Subscribe;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhouhao
 * @since 3.0
 */
public class SpringEventBusTest {
    SpringEventBus eventBus = new SpringEventBus();

    AtomicInteger counter = new AtomicInteger();

    @Test
    public void test() throws InterruptedException {
        System.out.println(Thread.currentThread().getName());
        eventBus.postProcessAfterInitialization(new Test2(), "test");
        eventBus.publish(eventBus);
        Thread.sleep(1000);
        Assert.assertEquals(counter.get(), 3);
    }

    public class Test2 {
        @Subscribe(mode = EventMode.SYNC)
        public void test1(SpringEventBus eventBus) {
            System.out.println(Thread.currentThread().getName());
            System.out.println(eventBus);
            counter.addAndGet(1);
        }

        @Subscribe(mode = EventMode.ASYNC, transaction = false)
        public void test2(SpringEventBus eventBus) {
            System.out.println(Thread.currentThread().getName());
            System.out.println(eventBus);
            counter.addAndGet(1);
        }

        @Subscribe(mode = EventMode.BACKGROUND, transaction = false)
        public void test3(SpringEventBus eventBus) {
            System.out.println(Thread.currentThread().getName());
            System.out.println(eventBus);
            counter.addAndGet(1);
        }
    }
}
