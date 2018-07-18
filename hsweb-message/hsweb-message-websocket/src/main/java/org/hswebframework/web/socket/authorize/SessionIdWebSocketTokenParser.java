package org.hswebframework.web.socket.authorize;

import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

public class SessionIdWebSocketTokenParser implements WebSocketTokenParser {
    @Override
    public String parseToken(WebSocketSession session) {
        HttpHeaders headers = session.getHandshakeHeaders();
        List<String> cookies = headers.get("Cookie");
        if (cookies == null || cookies.isEmpty()) {
            return null;
        }
        String[] cookie = cookies.get(0).split("[;]");
        Map<String, Set<String>> sessionId = new HashMap<>();
        for (String aCookie : cookie) {
            String[] tmp = aCookie.split("[=]");
            if (tmp.length == 2) {
                sessionId.computeIfAbsent(tmp[0].trim().toUpperCase(), k -> new HashSet<>())
                        .add(tmp[1].trim());
            }
        }
        return sessionId.getOrDefault("JSESSIONID", sessionId.getOrDefault("SESSIONID", new java.util.HashSet<>())).stream().findFirst().orElse(null);
    }
}
