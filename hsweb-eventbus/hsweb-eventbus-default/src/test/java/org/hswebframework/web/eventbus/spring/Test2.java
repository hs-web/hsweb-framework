package org.hswebframework.web.eventbus.spring;

import org.hswebframework.web.eventbus.annotation.EventMode;
import org.hswebframework.web.eventbus.annotation.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class Test2 {
    public static AtomicLong counter = new AtomicLong();

    @EventListener(mode = EventMode.SYNC)
    public void test1(SpringEventBus eventBus) {
//            System.out.println(Thread.currentThread().getName());
//            System.out.println(eventBus);
        counter.addAndGet(1);
    }

    @EventListener(mode = EventMode.ASYNC, transaction = false)
    public void test2(SpringEventBus eventBus) {
//            System.out.println(Thread.currentThread().getName());
//            System.out.println(eventBus);
        counter.addAndGet(1);
    }

    @EventListener(mode = EventMode.BACKGROUND, transaction = false)
    public void test3(SpringEventBus eventBus) {
//            System.out.println(Thread.currentThread().getName());
//            System.out.println(eventBus);
        counter.addAndGet(1);
    }
}