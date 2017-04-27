package org.hsweb.web.core.session.simple;

import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.session.AbstractHttpSessionManager;
import org.hsweb.web.core.utils.UnCheck;
import org.hsweb.web.core.utils.WebUtil;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Created by zhouhao on 16-5-27.
 */
public class SimpleHttpSessionManager extends AbstractHttpSessionManager {

    /**
     * httpSession存储器，sessionId:HttpSession
     */
    private static final ConcurrentMap<String, HttpSession> sessionStorage = new ConcurrentHashMap<>();

    /**
     * 用户ID与session管理存储器，userId:HttpSession
     */
    private static final ConcurrentMap<String, HttpSession> userSessionStorage = new ConcurrentHashMap<>();

    @Override
    public HttpSession getSessionBySessionId(String sessionId) {
        return sessionStorage.get(sessionId);
    }

    @Override
    public Set<User> tryGetAllUser() {
        return userSessionStorage.values().stream()
                .map(httpSession -> UnCheck.forNull(() -> (User) httpSession.getAttribute("user")))
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public User getUserBySessionId(String sessionId) {
        if (sessionId == null) return null;
        HttpSession session = sessionStorage.get(sessionId);
        return session == null ? null : WebUtil.getLoginUser(session);
    }

    @Override
    public String getSessionIdByUserId(String userId) {
        HttpSession session = userSessionStorage.get(userId);
        if (session != null) {
            User user = WebUtil.getLoginUser(session);
            if (user == null) {
                userSessionStorage.remove(userId);
                return null;
            }
            return session.getId();
        }
        return null;
    }

    @Override
    public void removeUser(String userId) {
        HttpSession session = userSessionStorage.get(userId);
        if (session != null) {
            sessionStorage.remove(session.getId());
            userSessionStorage.remove(userId);
            Exception exception = null;
            try {
                onUserLoginOut(userId, session);
            } catch (Exception ignored) {
                exception = ignored;
            }
            try {
                session.removeAttribute("user");
            } catch (Exception ignored) {
                // do noting
            }
            if (null != exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    @Override
    public void removeSession(String sessionId) {
        HttpSession session = sessionStorage.get(sessionId);
        if (session != null) {
            User user = WebUtil.getLoginUser(session);
            if (user != null) {
                userSessionStorage.remove(user.getId());
                onUserLoginOut(user.getId(), session);
            }
            sessionStorage.remove(sessionId);
        }
    }

    @Override
    public void addUser(User user, HttpSession session) {
        removeUser(user.getId());//踢出已经登陆
        sessionStorage.put(session.getId(), session);
        userSessionStorage.put(user.getId(), session);
        session.setAttribute("user", user);
        onUserLogin(user, session);
    }

    @Override
    public Set<String> getUserIdList() {
        return new HashSet<>(userSessionStorage.keySet());
    }

    @Override
    public int getUserTotal() {
        return userSessionStorage.size();
    }

    @Override
    public Set<String> getSessionIdList() {
        return new HashSet<>(sessionStorage.keySet());
    }

    @Override
    public boolean isLogin(String userId) {
        return userSessionStorage.containsKey(userId);
    }
}
