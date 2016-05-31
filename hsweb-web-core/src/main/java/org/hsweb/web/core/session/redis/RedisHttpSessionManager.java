package org.hsweb.web.core.session.redis;

import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.session.HttpSessionManager;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.ExpiringSession;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;

import javax.servlet.http.HttpSession;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by zhouhao on 16-5-27.
 */
public class RedisHttpSessionManager implements HttpSessionManager {

    private RedisTemplate sessionRedisTemplate;

    private RedisOperationsSessionRepository redisOperationsSessionRepository;

    @Override
    public Set<User> tryGetAllUser() {
        return (Set<User>) sessionRedisTemplate.execute((RedisCallback<Set<User>>) connection -> {
            Set<byte[]> keys = connection.keys("spring:session:sessions:*".getBytes());
            return keys.stream().map(key -> {
                String sessionId = new String(key).split("[:]")[3];
                ExpiringSession expiringSession = redisOperationsSessionRepository.getSession(sessionId);
                return (User) expiringSession.getAttribute("user");
            }).filter(user -> user != null).collect(Collectors.toSet());
        });
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
    public void removeUser(String userId) {
        sessionRedisTemplate.execute((RedisCallback) connection -> {
            String key = "http.session.user:" + userId;
            String sessionId = getSessionIdByUserId(userId);
            removeSession(sessionId);
            return connection.del(key.getBytes());
        });
    }

    @Override
    public void removeSession(String sessionId) {
        sessionRedisTemplate.execute((RedisCallback) connection ->
                        connection.del(("spring:session:sessions:" + sessionId).getBytes())
        );
    }

    @Override
    public void addUser(User user, HttpSession session) {
        removeUser(user.getId());
        sessionRedisTemplate.execute((RedisCallback) connection -> {
            String key = "http.session.user:" + user.getId();
            String value = session.getId();
            connection.set(key.getBytes(), value.getBytes());
            return null;
        });
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
}
