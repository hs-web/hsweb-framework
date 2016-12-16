package org.hsweb.web.message;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Component;

@Component
public class Consumer {


    @JmsListener(destination = "test2")
    public void receiveQueue3(String text) {
        System.out.println("3:" + text);
    }

    @JmsListener(destination = "test2")
    public void receiveQueue4(String text) {
        System.out.println("4:" + text);
    }

}