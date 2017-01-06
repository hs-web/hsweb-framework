package org.hswebframework.web.message.jms;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer2 {


    @JmsListener(destination = "test2", subscription = "test2")
    public void receiveQueue4(String text) {
        System.out.println("2:" + text);
    }

}