package org.hswebframework.web.eventbus.spring;

import org.hswebframework.web.eventbus.annotation.EventMode;
import org.hswebframework.web.eventbus.annotation.Subscribe;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class Test2 {
    public static AtomicLong counter = new AtomicLong();

    @Subscribe(mode = EventMode.SYNC)
    public void test1(SpringEventBus eventBus) {
//            System.out.println(Thread.currentThread().getName());
//            System.out.println(eventBus);
        counter.addAndGet(1);
    }

    @Subscribe(mode = EventMode.ASYNC, transaction = false)
    public void test2(SpringEventBus eventBus) {
//            System.out.println(Thread.currentThread().getName());
//            System.out.println(eventBus);
        counter.addAndGet(1);
    }

    @Subscribe(mode = EventMode.BACKGROUND, transaction = false)
    public void test3(SpringEventBus eventBus) {
//            System.out.println(Thread.currentThread().getName());
//            System.out.println(eventBus);
        counter.addAndGet(1);
    }
}