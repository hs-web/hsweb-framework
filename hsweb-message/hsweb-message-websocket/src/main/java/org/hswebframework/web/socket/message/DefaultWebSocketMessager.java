package org.hswebframework.web.socket.message;

import org.hswebframework.web.message.MessageSubscribe;
import org.hswebframework.web.message.Messager;
import org.hswebframework.web.message.support.ObjectMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        this.messager = messager;
    }

    // command,   userId,     sessionId
    private final Map<String, Map<String, Map<String, MessageSubscribeSession>>> store = new ConcurrentHashMap<>(32);

    @Override
    public void onSessionConnect(WebSocketSession session) {

    }

    @Override
    public void onSessionClose(WebSocketSession session) {

    }

    @Override
    public void publish(String toUser, WebSocketMessage message) {
        messager.publish(object(message))
                .to(user(toUser))
                .send();
    }

    private Map<String, MessageSubscribeSession> getSubSession(String command, String userId) {
        return store.computeIfAbsent(command, cmd -> new ConcurrentHashMap<>(128))
                .computeIfAbsent(userId, uid -> new ConcurrentHashMap<>());
    }

    @Override
    public boolean subscribe(String command, String userId, WebSocketSession socketSession) {
        Map<String, MessageSubscribeSession> subscribeSessionStore = getSubSession(command, userId);
        subscribeSessionStore.computeIfAbsent(socketSession.getId(), sessionId -> {
            MessageSubscribe<ObjectMessage<WebSocketMessage>> subscribe = messager.subscribe(user(userId));
            subscribe.onMessage(message -> {
                try {
                    if (!socketSession.isOpen()) {
                        deSubscribe(command, userId, socketSession);
                    }
                    socketSession.sendMessage(new TextMessage(((ObjectMessage) message).getObject().toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return new MessageSubscribeSession(subscribe, socketSession);
        });
        return false;
    }

    @Override
    public boolean deSubscribe(String command, String userId, WebSocketSession socketSession) {
        Map<String, MessageSubscribeSession> subscribeSessionStore = getSubSession(command, userId);
        MessageSubscribeSession subscribeSession = subscribeSessionStore.get(socketSession.getId());
        if (null != subscribeSession) {
            subscribeSession.getSubscribe().cancel();
            subscribeSessionStore.remove(socketSession.getId());
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
    }
}
