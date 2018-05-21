package org.hswebframework.web.message.memory;

import org.hswebframework.web.message.Message;
import org.hswebframework.web.message.MessagePublish;
import org.hswebframework.web.message.MessageSubject;
import org.hswebframework.web.message.support.MultipleQueueMessageSubject;
import org.hswebframework.web.message.support.QueueMessageSubject;
import org.hswebframework.web.message.support.TopicMessageSubject;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author zhouhao
 */
public class MemoryPublish implements MessagePublish {
    private MessageSubject subject;

    private Function<String, MemoryMessager.QueueConsumer<Message>> queueGetter;
    private Function<String, MemoryTopic<Message>>                  topicGetter;
    private Message                                                 message;

    public MemoryPublish(Function<String, MemoryMessager.QueueConsumer<Message>> queueGetter
            , Function<String, MemoryTopic<Message>> topicGetter, Message message) {
        this.queueGetter = queueGetter;
        this.topicGetter = topicGetter;
        this.message = message;
    }

    @Override
    public MessagePublish to(MessageSubject subject) {
        this.subject = subject;
        return this;
    }

    private static Random random = new Random();

    private void pubQueue(String name) {
        final MemoryMessager.QueueConsumer<Message> queueConsumer = queueGetter.apply(name);
        queueConsumer.lock.readLock().lock();
        try {
            int size = queueConsumer.consumers.size();
            if (size > 0) {
                queueConsumer.consumers.get(random.nextInt(size)).accept(message);
            }
        } finally {
            queueConsumer.lock.readLock().unlock();
        }
    }

    @Override
    public void send() {
        Objects.requireNonNull(subject);
        if (subject instanceof QueueMessageSubject) {
            pubQueue(((QueueMessageSubject) subject).getQueueName());
        }
        if (subject instanceof MultipleQueueMessageSubject) {
            ((MultipleQueueMessageSubject) subject).getQueueName().forEach(this::pubQueue);
        }
        if (subject instanceof TopicMessageSubject) {
            topicGetter.apply(((TopicMessageSubject) subject).getTopic()).publish(message);
        }
    }
}
