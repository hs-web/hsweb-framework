package org.hswebframework.web.socket.authorize;

import org.springframework.web.socket.WebSocketSession;

import java.util.List;

public class XAccessTokenParser implements WebSocketTokenParser {
    @Override
    public String parseToken(WebSocketSession session) {
        List<String> tokens = session.getHandshakeHeaders().get("x-access-token");
        return tokens == null || tokens.isEmpty() ? null : tokens.get(0);
    }
}
