package org.hsweb.web.socket.cmd;

import org.springframework.web.socket.WebSocketSession;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * websocket命令
 * Created by 浩 on 2016-01-19 0019.
 */
public class CMD {

    private String cmd;

    /**
     * 发送命令的会话
     */
    private transient WebSocketSession session;

    /**
     * 命令参数
     */
    private Map<String, Object> params = new LinkedHashMap<>();

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public String toString() {
        return "CMD{" +
                "cmd='" + cmd + '\'' +
                ", session=" + session +
                ", params=" + params +
                '}';
    }
}
