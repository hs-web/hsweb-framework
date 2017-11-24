package org.hswebframework.web.socket.authorize;

import org.springframework.web.socket.WebSocketSession;

public interface WebSocketTokenParser {
    String parseToken(WebSocketSession session);
}
