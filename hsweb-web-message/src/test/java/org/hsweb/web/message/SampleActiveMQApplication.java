package org.hsweb.web.message;

import javax.jms.Queue;

import org.apache.activemq.command.ActiveMQQueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class SampleActiveMQApplication {

	@Bean
    public Queue queue() {
		return new ActiveMQQueue("test");
	}

}