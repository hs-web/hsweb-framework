package org.hsweb.web.socket.message;

import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.exception.AuthorizeException;
import org.hsweb.web.core.session.HttpSessionManager;
import org.hsweb.web.socket.utils.SessionUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhouhao on 16-5-29.
 */
public class SimpleWebSocketMessageManager implements WebSocketMessageManager {

    private static final ConcurrentMap<String, Map<String, WebSocketSession>> session_map = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Subscribe> subscribe_map = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Map<String, Queue<WebSocketMessage>>> message_map = new ConcurrentHashMap<>();

    private HttpSessionManager httpSessionManager;

    public void setHttpSessionManager(HttpSessionManager httpSessionManager) {
        this.httpSessionManager = httpSessionManager;
    }

    public Map<String, WebSocketSession> getSessionMap(String userId) {
        Map<String, WebSocketSession> map = session_map.get(userId);
        if (map == null) {
            map = Collections.synchronizedMap(new HashMap<>());
            session_map.put(userId, map);
        }
        return map;
    }

    @Override
    public boolean publish(WebSocketMessage message) throws IOException {
        String to = message.getTo();
        Subscribe subscribe = subscribe_map.get(to);
        Map<String, WebSocketSession> socketSession = getSessionMap(message.getTo());
        if (!socketSession.isEmpty() && subscribe != null) {
            if (message.getSessionId() == null)
                socketSession.values().forEach(session -> {
                    try {
                        if (subscribe.getTopic(session.getId()).contains(message.getType()))
                            session.sendMessage(new TextMessage(message.toString()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        saveMessage(message);
                    }
                });
            else {
                WebSocketSession session = socketSession.get(message.getSessionId());
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(message.toString()));
                }

            }
            return true;
        }
        return false;
    }

    protected Queue<WebSocketMessage> getMessageQueue(String userId, String type) {
        Map<String, Queue<WebSocketMessage>> message_type_map = message_map.get(userId);
        if (message_type_map == null) {
            message_type_map = new ConcurrentHashMap<>();
            message_map.putIfAbsent(userId, message_type_map);
        }
        Queue<WebSocketMessage> queue = message_type_map.get(type);
        if (queue == null) {
            queue = new ConcurrentLinkedQueue<>();
            message_type_map.putIfAbsent(type, queue);
        }
        return queue;
    }

    protected void saveMessage(WebSocketMessage message) {
        getMessageQueue(message.getTo(), message.getType()).offer(message);
    }

    @Override
    public boolean deSubscribe(String type, String userId, WebSocketSession socketSession) {
        return getSubscribe(userId).getTopic(socketSession.getId()).remove(type);
    }

    protected Subscribe getSubscribe(String userId) {
        Subscribe subscribe = subscribe_map.get(userId);
        synchronized (subscribe_map) {
            if (subscribe == null) {
                subscribe = new Subscribe();
                subscribe.setUserId(userId);
                subscribe_map.put(userId, subscribe);
            }
        }
        return subscribe;
    }

    @Override
    public boolean subscribe(String type, String userId, WebSocketSession socketSession) {
        getSubscribe(userId).getTopic(socketSession.getId()).add(type);
        //推送未读消息
        Queue<WebSocketMessage> queue = getMessageQueue(userId, type);
        while (!queue.isEmpty()) {
            try {
                publish(queue.poll());
            } catch (IOException e) {
            }
        }
        return true;
    }

    class Subscribe {
        private String userId;
        private Map<String, Set<String>> topic = Collections.synchronizedMap(new HashMap<>());

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public void cancelTopic(String sessionId) {
            topic.remove(sessionId);
        }

        public Set<String> getTopic(String sessionId) {
            Set<String> tp = topic.get(sessionId);
            if (tp == null) {
                tp = Collections.synchronizedSet(new HashSet<>());
                topic.putIfAbsent(sessionId, tp);
            }
            return tp;
        }

    }

    @Override
    public void onSessionConnect(WebSocketSession session) throws Exception {
        User user = getUser(session);
        if (user == null) {
            throw new AuthorizeException("未登录");
        }
        getSessionMap(user.getId()).put(session.getId(), session);
    }

    @Override
    public void onSessionClose(WebSocketSession session) throws Exception {
        User user = getUser(session);
        if (user == null) {
            return;
        }
        Subscribe subscribe = subscribe_map.get(user.getId());
        if (subscribe != null)
            subscribe.cancelTopic(session.getId());
        getSessionMap(user.getId()).remove(session.getId());
    }

    protected User getUser(WebSocketSession session) {
        return SessionUtils.getUser(session, httpSessionManager);
    }
}
