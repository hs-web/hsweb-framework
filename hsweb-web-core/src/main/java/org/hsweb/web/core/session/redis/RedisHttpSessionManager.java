package org.hsweb.web.core.session.redis;

import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.session.AbstractHttpSessionManager;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.ExpiringSession;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by zhouhao on 16-5-27.
 */
public class RedisHttpSessionManager extends AbstractHttpSessionManager {

    private RedisTemplate sessionRedisTemplate;

    private RedisOperationsSessionRepository redisOperationsSessionRepository;

    @Override
    public Set<User> tryGetAllUser() {
        return (Set<User>) sessionRedisTemplate.keys("spring:session:sessions:*")
                .stream().map(key -> {
                    String sessionId = String.valueOf(key).split("[:]")[3];
                    ExpiringSession expiringSession = redisOperationsSessionRepository.getSession(sessionId);
                    return expiringSession.getAttribute("user");
                }).filter(user -> user != null).collect(Collectors.toSet());
    }

    @Override
    public User getUserBySessionId(String sessionId) {
        if (sessionId == null) return null;
        ExpiringSession redisSession = redisOperationsSessionRepository.getSession(sessionId);
        if (redisSession != null) {
            return (User) redisSession.getAttribute("user");
        }
        return null;
    }

    @Override
    public String getSessionIdByUserId(String userId) {
        return (String) sessionRedisTemplate.execute((RedisCallback<String>) connection -> {
            String key = "http.session.user:" + userId;
            byte[] sessionId = connection.get(key.getBytes());
            if (sessionId == null) return null;
            return new String(sessionId);
        });
    }

    @Override
    public HttpSession getSessionBySessionId(String sessionId) {
        ExpiringSession redisSession = redisOperationsSessionRepository.getSession(sessionId);
        if (redisSession == null) return null;
        return new HttpSessionWrapper(redisSession);
    }

    @Override
    public void removeUser(String userId) {
        String key = "http.session.user:" + userId;
        String sessionId = getSessionIdByUserId(userId);
        ExpiringSession redisSession = redisOperationsSessionRepository.getSession(sessionId);
        HttpSession session = new HttpSessionWrapper(redisSession);
        onUserLoginOut(userId, session);
        removeSession(sessionId);
        sessionRedisTemplate.delete(key);
    }

    @Override
    public void removeSession(String sessionId) {
        sessionRedisTemplate.delete("spring:session:sessions:".concat(sessionId));
    }

    @Override
    public void addUser(User user, HttpSession session) {
        removeUser(user.getId());
        String key = "http.session.user:" + user.getId();
        String value = session.getId();
        sessionRedisTemplate.opsForValue().set(key, value);
        onUserLogin(user, session);
    }

    @Override
    public Set<String> getUserIdList() {
        return (Set<String>) sessionRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<byte[]> keys = connection.keys("http.session.user:*".getBytes());
            return keys.stream().map(key -> {
                String sessionId = "spring:session:sessions:" + new String(connection.get(key));
                String userId = new String(key).split("[:]")[1];
                if (!connection.exists(sessionId.getBytes())) {
                    removeUser(userId);
                    return null;
                }
                return userId;
            }).filter(key -> key != null).collect(Collectors.toSet());
        });
    }

    @Override
    public int getUserTotal() {
        return getUserIdList().size();
    }

    @Override
    public Set<String> getSessionIdList() {
        Set<String> strings = (Set) sessionRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<byte[]> keys = connection.keys("http.session.user:*".getBytes());
            return keys.stream().map(key -> {
                String sessionId = new String(connection.get(key));
                return sessionId;
            }).collect(Collectors.toSet());
        });
        return strings;
    }

    @Override
    public boolean isLogin(String userId) {
        return (Boolean) sessionRedisTemplate.execute((RedisCallback) connection ->
                connection.exists(("http.session.user:" + userId).getBytes())
        );
    }

    public void setRedisOperationsSessionRepository(RedisOperationsSessionRepository redisOperationsSessionRepository) {
        this.redisOperationsSessionRepository = redisOperationsSessionRepository;
    }

    public void setSessionRedisTemplate(RedisTemplate sessionRedisTemplate) {
        this.sessionRedisTemplate = sessionRedisTemplate;
    }

    private final class HttpSessionWrapper implements HttpSession {
        private ExpiringSession session;
        private boolean         invalidated;
        private boolean         old;

        public HttpSessionWrapper(ExpiringSession session) {
            this.session = session;
        }

        public long getCreationTime() {
            return session.getCreationTime();
        }

        public String getId() {
            return session.getId();
        }

        public long getLastAccessedTime() {
            checkState();
            return session.getLastAccessedTime();
        }

        public ServletContext getServletContext() {
            return null;
        }

        public void setMaxInactiveInterval(int interval) {
            session.setMaxInactiveIntervalInSeconds(interval);
        }

        public int getMaxInactiveInterval() {
            return session.getMaxInactiveIntervalInSeconds();
        }

        @SuppressWarnings("deprecation")
        public HttpSessionContext getSessionContext() {
            return null;
        }

        public Object getAttribute(String name) {
            checkState();
            return session.getAttribute(name);
        }

        public Object getValue(String name) {
            return getAttribute(name);
        }

        public Enumeration<String> getAttributeNames() {
            checkState();
            return Collections.enumeration(session.getAttributeNames());
        }

        public String[] getValueNames() {
            checkState();
            Set<String> attrs = session.getAttributeNames();
            return attrs.toArray(new String[0]);
        }

        public void setAttribute(String name, Object value) {
            checkState();
            session.setAttribute(name, value);
        }

        public void putValue(String name, Object value) {
            setAttribute(name, value);
        }

        public void removeAttribute(String name) {
            checkState();
            session.removeAttribute(name);
        }

        public void removeValue(String name) {
            removeAttribute(name);
        }

        public void invalidate() {
            checkState();
            invalidated = true;
        }

        public void setNew(boolean isNew) {
            this.old = !isNew;
        }

        public boolean isNew() {
            checkState();
            return !old;
        }

        private void checkState() {
            if (invalidated) {
                throw new IllegalStateException("The HttpSession has already be invalidated.");
            }
        }
    }
}
