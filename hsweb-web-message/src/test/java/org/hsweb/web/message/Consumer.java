package org.hsweb.web.message;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {

	@JmsListener(destination = "test")
    public void receiveQueue(String text) {
		System.out.println(text);
	}

}