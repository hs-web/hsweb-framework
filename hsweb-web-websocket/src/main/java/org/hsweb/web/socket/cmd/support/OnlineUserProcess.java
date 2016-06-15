package org.hsweb.web.socket.cmd.support;

import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.session.HttpSessionManagerListener;
import org.hsweb.web.socket.cmd.CMD;
import org.hsweb.web.socket.message.WebSocketMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 通过websocket推送 在线人数
 * Created by zhouhao on 16-6-2.
 */
@Component
public class OnlineUserProcess extends AbstractCmdProcessor implements HttpSessionManagerListener {
    private Set<String> userList = Collections.synchronizedSet(new HashSet<>());

    @Override
    public String getName() {
        return "online";
    }

    @Override
    public void exec(CMD cmd) throws Exception {
        String type = (String) cmd.getParams().get("type");
        if (type == null) return;
        User user = getUser(cmd.getSession());
        if (user != null) {
            String callback = (String) cmd.getParams().getOrDefault("callback", "onlineUserTotal");
            webSocketMessageManager.subscribe(getName(), user.getId(), cmd.getSession());
            pushOnlineTotalToUser(user.getId(), callback);
            userList.add(user.getId());
        }
    }

    protected void pushOnlineTotalToUser(String userId, String callback) {
        int total = httpSessionManager.getUserTotal();
        WebSocketMessage message = new WebSocketMessage();
        message.setTo(userId);
        message.setFrom("system");
        message.setCallBack(callback);
        message.setContent(total);
        message.setType(getName());
        try {
            webSocketMessageManager.publish(message);
        } catch (Exception e) {
            logger.error("推送在线人数失败", e);
        }
    }

    protected void pushOnlineTotalToAllUser(String callback) {
        userList.forEach(userId -> pushOnlineTotalToUser(userId, callback));
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void onSessionConnect(WebSocketSession session) throws Exception {

    }

    @Override
    public void onSessionClose(WebSocketSession session) throws Exception {
        User user = getUser(session);
        if (user != null) {
            webSocketMessageManager.deSubscribe(getName(), user.getId(), session);
        }
    }

    @Override
    public void onUserLogin(User user, HttpSession session) {
        pushOnlineTotalToAllUser("onlineUserTotal");
    }

    @Override
    public void onUserLoginOut(String userId, HttpSession session) {
        userList.remove(userId);
        pushOnlineTotalToAllUser("onlineUserTotal");
    }
}
