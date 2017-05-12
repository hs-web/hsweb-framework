package org.hswebframework.web.socket.message;

import org.hswebframework.web.socket.WebSocketSessionListener;
import org.springframework.web.socket.WebSocketSession;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface WebSocketMessager extends WebSocketSessionListener {
    void publish(String toUser, WebSocketMessage message);

    boolean subscribe(String command, String userId, WebSocketSession socketSession);

    boolean deSubscribe(String command, String userId, WebSocketSession socketSession);

}
