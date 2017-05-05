package org.hsweb.web.socket.utils;

import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.session.HttpSessionManager;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.function.Function;

/**
 * Created by zhouhao on 16-5-30.
 */
public class SessionUtils {
    public static User getUser(WebSocketSession session, HttpSessionManager sessionManager) {
        if (sessionManager == null) return null;
        User user = ((User) session.getAttributes().get("user"));
        if (user != null) return user;
        HttpHeaders headers = session.getHandshakeHeaders();
        List<String> cookies = headers.get("Cookie");
        if (cookies == null || cookies.isEmpty()) {
            return null;
        }
        String[] cookie = cookies.get(0).split("[;]");
        Map<String, Set<String>> sessionId = new HashMap<>();
        for (String aCookie : cookie) {
            String[] tmp = aCookie.split("[=]");
            if (tmp.length == 2)
                sessionId.computeIfAbsent(tmp[0].trim(), k -> new HashSet<>())
                        .add(tmp[1].trim());
        }

        Function<Set<String>, Optional<User>> userGetter = set ->
                set == null ? Optional.empty() : set.stream()
                        .map(sessionManager::getUserBySessionId)
                        .filter(Objects::nonNull).findFirst();

        return userGetter.apply(sessionId.get("SESSION"))
                .orElseGet(() -> userGetter.apply(sessionId.get("JSESSIONID")).orElse(null));

    }
}
