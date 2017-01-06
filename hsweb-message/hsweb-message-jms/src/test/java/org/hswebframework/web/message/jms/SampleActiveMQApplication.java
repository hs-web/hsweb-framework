package org.hswebframework.web.message.jms;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

import javax.jms.*;

@SpringBootApplication
@EnableJms
public class SampleActiveMQApplication {


//    @Bean
//    public Queue queue() {
//        return new ActiveMQQueue("test");
//    }
//
//    @Bean
//    public Topic topic() {
//        ActiveMQTopic topic = new ActiveMQTopic("test2");
//        return topic;
//    }

}