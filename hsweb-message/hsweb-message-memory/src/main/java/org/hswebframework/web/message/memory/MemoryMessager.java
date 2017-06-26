package org.hswebframework.web.message.memory;

import org.hswebframework.web.message.*;
import org.hswebframework.web.message.support.QueueMessageSubject;
import org.hswebframework.web.message.support.TopicMessageSubject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class MemoryMessager implements Messager {

    private Map<String, MemoryTopic<? extends Message>> topicStore = new ConcurrentHashMap<>(256);

    private Map<String, QueueConsumer<? extends Message>> queueStore = new ConcurrentHashMap<>(512);

    private Executor executor;

    public MemoryMessager(Executor executor) {
        this.executor = executor;
    }

    public MemoryMessager() {
        this(Executors.newCachedThreadPool());
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public MessagePublish publish(Message message) {
        return new MemoryPublish(this::getQueue, this::getTopic, message);
    }

    @SuppressWarnings("unchecked")
    private <M extends Message> QueueConsumer<M> getQueue(String name) {
        return (QueueConsumer) queueStore
                .computeIfAbsent(name, queueName -> new QueueConsumer<>());
    }

    @SuppressWarnings("unchecked")
    public <M extends Message> MemoryTopic<M> getTopic(String name) {
        return (MemoryTopic) topicStore
                .computeIfAbsent(name, topic -> new MemoryTopic<>());
    }

    @Override
    public <M extends Message> MessageSubscribe<M> subscribe(MessageSubject subject) {
        if (subject instanceof QueueMessageSubject) {
            QueueConsumer<M> queue = getQueue(((QueueMessageSubject) subject).getQueueName());
            return new MessageSubscribe<M>() {
                private List<Consumer<M>> consumers = new ArrayList<>();
                private Consumer<M> consumer = m -> consumers.forEach(cons -> cons.accept(m));

                {
                    queue.lock.writeLock().lock();
                    try {
                        queue.consumers.add(consumer);
                    } finally {
                        queue.lock.writeLock().unlock();
                    }
                }

                @Override
                public MessageSubscribe<M> onMessage(Consumer<M> consumer) {
                    consumers.add(consumer);
                    return this;
                }

                @Override
                public void cancel() {
                    boolean lockSuccess = true;
                    try {
                        queue.lock.writeLock().tryLock(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        lockSuccess = false;
                    }
                    try {
                        queue.consumers.remove(consumer);
                    } finally {
                        try {
                            queue.lock.writeLock().unlock();
                        } catch (Exception e) {
                        }
                    }
                }
            };
        } else if (subject instanceof TopicMessageSubject) {
            return new MemoryTopicSubscribe<>(topicStore
                    .computeIfAbsent(((TopicMessageSubject) subject).getTopic(), topic -> new MemoryTopic<>()));
        }
        throw new UnsupportedOperationException(subject.getClass().getName());
    }

    class QueueConsumer<M extends Message> {
        final List<Consumer<M>> consumers = new ArrayList<>();
        final ReadWriteLock     lock      = new ReentrantReadWriteLock();
    }
}
