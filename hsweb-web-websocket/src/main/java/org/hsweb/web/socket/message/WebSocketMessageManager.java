package org.hsweb.web.socket.message;

import org.hsweb.web.socket.WebSocketSessionListener;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * websocket消息管理器，用于使用websocket进行消息推送
 * Created by 浩 on 2016-01-19 0019.
 */
public interface WebSocketMessageManager extends WebSocketSessionListener {

    /**
     * 发送一个消息
     *
     * @param message 消息实例
     * @return 是否发送成功
     */
    boolean publish(WebSocketMessage message) throws IOException;

    boolean subscribe(String type, String userId,WebSocketSession socketSession);

    boolean deSubscribe(String type, String userId,WebSocketSession socketSession);
}
