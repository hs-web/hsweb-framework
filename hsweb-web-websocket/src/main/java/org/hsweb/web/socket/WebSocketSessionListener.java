package org.hsweb.web.socket;

import org.springframework.web.socket.WebSocketSession;

/**
 * Created by 浩 on 2016-01-19 0019.
 */
public interface WebSocketSessionListener {
    /**
     * 当session创建时，调用此方法
     *
     * @param session WebSocketSession 实例
     * @throws Exception
     */
    void onSessionConnect(WebSocketSession session) throws Exception;

    /**
     * 当session关闭时，调用此方法
     *
     * @param session WebSocketSession 实例
     * @throws Exception
     */
    void onSessionClose(WebSocketSession session) throws Exception;

}
