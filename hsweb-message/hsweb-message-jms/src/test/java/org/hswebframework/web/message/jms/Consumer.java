package org.hswebframework.web.message.jms;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Component;

@Component
public class Consumer {

    @JmsListener(destination = "test")
    public void receiveQueue(String text) {
        System.out.println("3:" + text);
    }

    @JmsListener(destination = "test")
    public void receiveQueue4(String text) {
        System.out.println("4:" + text);
    }

    @JmsListener(destination = "test2", subscription = "test2")
    public void receiveQueue3(String text) {
        System.out.println("1:" + text);
    }


}