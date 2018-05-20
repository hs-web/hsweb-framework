package org.hswebframework.web.message.memory;

import org.hswebframework.web.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author zhouhao
 */
class MemoryTopic<M extends Message> {
    private Map<String, List<Consumer<M>>> consumers = new ConcurrentHashMap<>();

    public void remove(String id) {
        consumers.remove(id);
    }

    public void subscribe(String id, Consumer<M> consumer) {
        consumers.computeIfAbsent(id, i -> new ArrayList<>())
                .add(consumer);
    }

    public void publish(M message) {
        consumers.values().stream().flatMap(List::stream).forEach(consumer -> consumer.accept(message));
    }
}
