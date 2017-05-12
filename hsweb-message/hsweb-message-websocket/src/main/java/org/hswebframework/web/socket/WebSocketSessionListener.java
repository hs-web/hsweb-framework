package org.hswebframework.web.socket;

import org.springframework.web.socket.WebSocketSession;

public interface WebSocketSessionListener {
    /**
     * 当session创建时，调用此方法
     *
     * @param session WebSocketSession 实例
     * @throws Exception
     */
    void onSessionConnect(WebSocketSession session);

    /**
     * 当session关闭时，调用此方法
     *
     * @param session WebSocketSession 实例
     * @throws Exception
     */
    void onSessionClose(WebSocketSession session);

}