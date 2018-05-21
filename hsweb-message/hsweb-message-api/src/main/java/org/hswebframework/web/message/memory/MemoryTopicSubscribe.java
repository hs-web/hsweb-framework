package org.hswebframework.web.message.memory;

import org.hswebframework.web.message.Message;
import org.hswebframework.web.message.MessageSubscribe;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author zhouhao
 */
public class MemoryTopicSubscribe<M extends Message> implements MessageSubscribe<M> {

    private MemoryTopic topic;

    private String id;

    public MemoryTopicSubscribe(MemoryTopic topic) {
        this.topic = topic;
        id = UUID.randomUUID().toString();
    }

    @Override
    public MessageSubscribe<M> onMessage(Consumer<M> consumer) {
        topic.subscribe(id, consumer);
        return this;
    }

    @Override
    public void cancel() {
        topic.remove(id);
    }

}
