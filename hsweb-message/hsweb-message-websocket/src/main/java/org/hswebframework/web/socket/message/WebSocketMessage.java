package org.hswebframework.web.socket.message;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * @author zhouhao
 */
public class WebSocketMessage implements Serializable {
    private static final long serialVersionUID = -1173161338949028545L;

    private String command;

    private int status;

    private String message;

    private Object result;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public WebSocketMessage() {
    }

    public WebSocketMessage(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public WebSocketMessage(int status, String message, Object result) {
        this.status = status;
        this.message = message;
        this.result = result;
    }
}
