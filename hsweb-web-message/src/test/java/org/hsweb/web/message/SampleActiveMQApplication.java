package org.hsweb.web.message;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Topic;

@SpringBootApplication
@EnableJms
public class SampleActiveMQApplication {

    @Bean
    public Queue queue() {
        return new ActiveMQQueue("test");
    }

    @Bean
    public Topic topic() {
        ActiveMQTopic topic = new ActiveMQTopic("test2");
        return topic;
    }

}