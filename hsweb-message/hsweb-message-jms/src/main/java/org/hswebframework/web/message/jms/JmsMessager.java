package org.hswebframework.web.message.jms;

import org.apache.activemq.command.ActiveMQQueue;
import org.hswebframework.web.message.*;
import org.hswebframework.web.message.support.QueueMessageSubject;
import org.hswebframework.web.message.support.TopicMessageSubject;
import org.springframework.jms.core.JmsTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class JmsMessager implements Messager {

    private JmsTemplate queueTemplate;

    private JmsTemplate topicTemplate;

    private Executor executor;

    public JmsMessager(JmsTemplate template) {
        this(template, Executors.newCachedThreadPool());
    }

    public JmsMessager(JmsTemplate template, Executor executor) {
        queueTemplate = new JmsTemplate(template.getConnectionFactory());
        queueTemplate.setDestinationResolver(template.getDestinationResolver());
        queueTemplate.setPubSubDomain(false);
        topicTemplate = new JmsTemplate(template.getConnectionFactory());
        topicTemplate.setDestinationResolver(template.getDestinationResolver());
        topicTemplate.setPubSubDomain(true);
        this.executor = executor;
    }

    @Override
    public MessagePublish publish(Message message) {
        return new JmsMessagePublish(queueTemplate, topicTemplate, message);
    }

    @Override
    public <M extends Message> MessageSubscribe<M> subscribe(MessageSubject subject) {
        String subjectName = null;
        JmsTemplate template = null;
        if (subject instanceof QueueMessageSubject) {
            subjectName = ((QueueMessageSubject) subject).getQueueName();
            template = queueTemplate;
        } else if (subject instanceof TopicMessageSubject) {
            subjectName = ((TopicMessageSubject) subject).getTopic();
            template = topicTemplate;
        }
        if (null == subjectName) {
            throw new UnsupportedOperationException(subject.getClass().getName());
        }
        return new JmsMessageSubscribe<>(template, executor, subjectName);
    }
}
