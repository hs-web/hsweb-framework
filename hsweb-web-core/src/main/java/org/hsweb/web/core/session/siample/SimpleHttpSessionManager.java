package org.hsweb.web.core.session.siample;

import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.session.HttpSessionManager;
import org.hsweb.web.core.utils.WebUtil;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by zhouhao on 16-5-27.
 */
public class SimpleHttpSessionManager implements HttpSessionManager {

    /**
     * httpSession存储器，sessionId:HttpSession
     */
    private static final Map<String, HttpSession> sessionStorage = new ConcurrentHashMap<>();

    /**
     * 用户ID与session管理存储器，userId:HttpSession
     */
    private static final Map<String, HttpSession> userSessionStorage = new ConcurrentHashMap<>();

    @Override
    public Set<User> tryGetAllUser() {
        return userSessionStorage.values().stream().map(httpSession -> (User) httpSession.getAttribute("user"))
                .filter(user -> user != null).collect(Collectors.toSet());
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
            return user.getId();
        }
        return null;
    }

    @Override
    public void removeUser(String userId) {
        HttpSession session = userSessionStorage.get(userId);
        if (session != null) {
            try {
                session.removeAttribute("user");
            } catch (Exception e) {
            } finally {
                sessionStorage.remove(session.getId());
                userSessionStorage.remove(userId);
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
            }
            sessionStorage.remove(sessionId);
        }
    }

    @Override
    public void addUser(String userId, HttpSession session) {
        sessionStorage.put(session.getId(), session);
        removeUser(userId);//踢出已经登陆
        userSessionStorage.put(userId, session);
    }

    @Override
    public Set<String> getUserIdList() {
        return userSessionStorage.keySet();
    }

    @Override
    public int getUserTotal() {
        return userSessionStorage.size();
    }

    @Override
    public Set<String> getSessionIdList() {
        return sessionStorage.keySet();
    }

    @Override
    public boolean isLogin(String userId) {
        return userSessionStorage.containsKey(userId);
    }
}
