package org.hswebframework.web.message.jms;

import org.hswebframework.web.message.Message;
import org.hswebframework.web.message.MessageListener;
import org.hswebframework.web.message.MessageManager;
import org.springframework.jms.core.JmsTemplate;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class JmsMessageManager implements MessageManager {
    JmsTemplate jmsTemplate;

    @Override
    public void send(String toUser,String destination, Message message) {

    }

    @Override
    public void publish(String topic, Message message) {

    }

    @Override
    public <T extends Message> void subscribe(String topic, MessageListener<T> listener) {

    }

    @Override
    public void deSubscribe(String topic) {

    }
}
