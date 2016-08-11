package org.hsweb.web.core.session;

import org.hsweb.web.bean.po.user.User;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHttpSessionManager implements HttpSessionManager {

    private List<HttpSessionManagerListener> listeners = new ArrayList<>();

    protected void onUserLogin(User user,HttpSession session) {
        if (listeners != null) {
            listeners.forEach(listener -> listener.onUserLogin(user,session));
        }
    }

    protected void onUserLoginOut(String  userId,HttpSession session) {
        if (listeners != null) {
            listeners.forEach(listener -> listener.onUserLoginOut(userId,session));
        }
    }

    @Autowired(required = false)
    public void setListeners(List<HttpSessionManagerListener> listeners) {
        this.listeners = listeners;
    }

    public List<HttpSessionManagerListener> getListeners() {
        return listeners;
    }

    @Override
    public void addListener(HttpSessionManagerListener listener) {
        listeners.add(listener);
    }

}
