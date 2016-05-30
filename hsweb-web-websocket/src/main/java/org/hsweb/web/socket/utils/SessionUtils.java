package org.hsweb.web.socket.utils;

import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.session.HttpSessionManager;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouhao on 16-5-30.
 */
public class SessionUtils {
    public static User getUser(WebSocketSession session, HttpSessionManager sessionManager) {
        User user = ((User) session.getAttributes().get("user"));
        if (user != null) return user;
        HttpHeaders headers = session.getHandshakeHeaders();
        List<String> cookies = headers.get("Cookie");
        if (cookies == null || cookies.isEmpty()) {
            return null;
        }
        String[] cookie = cookies.get(0).split("[;]");
        Map<String, Object> sessionId = new HashMap<>();
        for (int i = 0; i < cookie.length; i++) {
            String[] tmp = cookie[i].split("[=]");
            if (tmp.length == 2)
                sessionId.put(tmp[0].trim(), tmp[1].trim());
        }
        user = sessionManager.getUserBySessionId((String) sessionId.get("SESSION"));
        if (user == null) {
            user = sessionManager.getUserBySessionId((String) sessionId.get("JSESSIONID"));
        }
        return user;
    }
}
