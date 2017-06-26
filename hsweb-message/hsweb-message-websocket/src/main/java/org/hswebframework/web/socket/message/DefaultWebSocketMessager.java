package org.hswebframework.web.socket.message;

import org.hswebframework.web.concurrent.counter.Counter;
import org.hswebframework.web.concurrent.counter.CounterManager;
import org.hswebframework.web.concurrent.counter.SimpleCounterManager;
import org.hswebframework.web.message.MessageSubscribe;
import org.hswebframework.web.message.Messager;
import org.hswebframework.web.message.support.ObjectMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.hswebframework.web.message.builder.StaticMessageBuilder.object;
import static org.hswebframework.web.message.builder.StaticMessageSubjectBuilder.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DefaultWebSocketMessager implements WebSocketMessager {

    private Messager messager;

    public DefaultWebSocketMessager(Messager messager) {
        this(messager, new SimpleCounterManager());
    }

    public DefaultWebSocketMessager(Messager messager, CounterManager counterManager) {
        this.messager = messager;
        this.counterManager = counterManager == null ? new SimpleCounterManager() : counterManager;
    }

    //              command,   type,     sessionId
    private final Map<String, Map<String, Map<String, MessageSubscribeSession>>> store = new ConcurrentHashMap<>(32);

    private CounterManager counterManager = new SimpleCounterManager();


    @Override
    public void onSessionConnect(WebSocketSession session) {

    }

    private String getSubTotalKey(String command, String type) {
        return "sub_".concat(command)
                .concat("_")
                .concat(type)
                .concat("_total");
    }

    @Override
    public int getSubscribeTotal(String command, String type) {
        return (int) counterManager.getCounter(getSubTotalKey(command, type)).get();
    }

    @Override
    public void onSessionClose(WebSocketSession session) {
        store.values()  //command
                .stream().map(Map::values).flatMap(Collection::stream)
                .map(sessionStore -> sessionStore.get(session.getId()))
                .filter(Objects::nonNull)
                .forEach(MessageSubscribeSession::cancel);
    }

    @Override
    public void publish(String command, String type, WebSocketMessage message) {
        messager.publish(object(message))
                .to(TYPE_QUEUE.equals(type) ? queue("queue_" + command) : topic("topic_" + command))
                .send();
    }

    private Map<String, MessageSubscribeSession> getSubSession(String command, String type) {
        return store.computeIfAbsent(command, cmd -> new ConcurrentHashMap<>(128))
                .computeIfAbsent(type, t -> new ConcurrentHashMap<>());
    }

    @Override
    public boolean subscribe(String command, String type, WebSocketSession socketSession) {
        Map<String, MessageSubscribeSession> subscribeSessionStore = getSubSession(command, type);
        subscribeSessionStore.computeIfAbsent(socketSession.getId(), sessionId -> {
            MessageSubscribe<ObjectMessage<WebSocketMessage>> subscribe = messager
                    .subscribe(TYPE_QUEUE.equals(type) ? queue("queue_" + command) : topic("topic_" + command));
            subscribe.onMessage(message -> {
                try {
                    if (!socketSession.isOpen()) {
                        deSubscribe(command, type, socketSession);
                        return;
                    }
                    socketSession.sendMessage(new TextMessage(((ObjectMessage) message).getObject().toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return new MessageSubscribeSession(subscribe, socketSession) {
                @Override
                public void cancel() {
                    super.cancel();
                    counterManager.getCounter(getSubTotalKey(command, type)).decrement();
                }
            };
        });
        counterManager.getCounter(getSubTotalKey(command, type)).increment();
        return true;
    }

    @Override
    public boolean deSubscribe(String command, String type, WebSocketSession socketSession) {
        Map<String, MessageSubscribeSession> subscribeSessionStore = getSubSession(command, type);
        MessageSubscribeSession subscribeSession = subscribeSessionStore.get(socketSession.getId());
        if (null != subscribeSession) {
            subscribeSession.getSubscribe().cancel();
            subscribeSessionStore.remove(socketSession.getId());
            counterManager.getCounter(getSubTotalKey(command, type)).decrement();
            return true;
        }
        return false;
    }

    public class MessageSubscribeSession {
        private MessageSubscribe<ObjectMessage<WebSocketMessage>> subscribe;

        private WebSocketSession session;

        public MessageSubscribeSession(MessageSubscribe<ObjectMessage<WebSocketMessage>> subscribe, WebSocketSession session) {
            this.subscribe = subscribe;
            this.session = session;
        }

        public MessageSubscribe<ObjectMessage<WebSocketMessage>> getSubscribe() {
            return subscribe;
        }

        public void setSubscribe(MessageSubscribe<ObjectMessage<WebSocketMessage>> subscribe) {
            this.subscribe = subscribe;
        }

        public WebSocketSession getSession() {
            return session;
        }

        public void setSession(WebSocketSession session) {
            this.session = session;
        }

        public void cancel() {
            subscribe.cancel();
        }
    }
}
