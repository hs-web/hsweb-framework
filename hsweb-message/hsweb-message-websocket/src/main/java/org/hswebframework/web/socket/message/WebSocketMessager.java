package org.hswebframework.web.socket.message;

import org.hswebframework.web.socket.WebSocketSessionListener;
import org.springframework.web.socket.WebSocketSession;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface WebSocketMessager extends WebSocketSessionListener {

    String TYPE_QUEUE = "queue";

    String TYPE_TOPIC = "topic";

    default void publishQueue(String command, WebSocketMessage message) {
        publish(command, TYPE_QUEUE, message);
    }

    default void publishTopic(String command, WebSocketMessage message) {
        publish(command, TYPE_TOPIC, message);
    }

    void publish(String command, String type, WebSocketMessage message);

    int getSubscribeTotal(String command, String type);

    boolean subscribe(String command, String type, WebSocketSession socketSession);

    default boolean subscribeQueue(String command, WebSocketSession socketSession) {
        return subscribe(command, TYPE_QUEUE, socketSession);
    }

    default boolean subscribeTopic(String command, WebSocketSession socketSession) {
        return subscribe(command, TYPE_TOPIC, socketSession);
    }

    boolean deSubscribe(String command, String type, WebSocketSession socketSession);

    default boolean deSubscribeQueue(String command, WebSocketSession socketSession) {
        return deSubscribe(command, TYPE_QUEUE, socketSession);
    }

    default boolean deSubscribeTopic(String command, WebSocketSession socketSession) {
        return deSubscribe(command, TYPE_TOPIC, socketSession);
    }
}
