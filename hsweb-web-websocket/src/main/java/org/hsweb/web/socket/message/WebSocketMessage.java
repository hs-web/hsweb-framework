package org.hsweb.web.socket.message;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.Date;

/**
 * websocket消息
 * Created by 浩 on 2016-01-19 0019.
 */
public class WebSocketMessage implements Serializable {
    private static final long serialVersionUID = 755525816405705645L;
    /**
     * 接收者
     */
    private String to;

    /**
     * 发送者
     */
    private String from;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private Object content;

    /**
     * 发送日期
     */
    private Date sendTime;

    private String sessionId;
    /**
     * 前端回掉,前端注册了此消息的回掉后，在接收到消息时，会自动触发该回掉
     */
    private String callBack;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getCallBack() {
        return callBack;
    }

    public void setCallBack(String callBack) {
        this.callBack = callBack;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
