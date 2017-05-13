package org.hswebframework.web.message.jms;

import org.hswebframework.web.message.Message;
import org.hswebframework.web.message.MessagePublish;
import org.hswebframework.web.message.MessageSubject;
import org.hswebframework.web.message.support.MultipleQueueMessageSubject;
import org.hswebframework.web.message.support.QueueMessageSubject;
import org.hswebframework.web.message.support.TopicMessageSubject;
import org.springframework.jms.core.JmsTemplate;

import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class JmsMessagePublish implements MessagePublish {
    private MessageSubject subject;

    private JmsTemplate queueTemplate;
    private JmsTemplate topicTemplate;
    private Message     message;

    public JmsMessagePublish(JmsTemplate queueTemplate, JmsTemplate topicTemplate, Message message) {
        this.queueTemplate = queueTemplate;
        this.topicTemplate = topicTemplate;
        this.message = message;
    }

    @Override
    public MessagePublish to(MessageSubject subject) {
        this.subject = subject;
        return this;
    }

    @Override
    public void send() {
        if (subject instanceof QueueMessageSubject) {
            queueTemplate.convertAndSend(((QueueMessageSubject) subject).getQueueName(), message);
        }
        if (subject instanceof MultipleQueueMessageSubject) {
            Set<String> queueNames = ((MultipleQueueMessageSubject) subject).getQueueName();
            queueNames.forEach(name -> queueTemplate.convertAndSend(name, message));
        }
        if (subject instanceof TopicMessageSubject) {
            topicTemplate.convertAndSend(((TopicMessageSubject) subject).getTopic(), message);
        }
    }
}
