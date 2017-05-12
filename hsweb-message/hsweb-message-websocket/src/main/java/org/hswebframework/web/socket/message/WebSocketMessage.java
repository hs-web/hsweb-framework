package org.hswebframework.web.socket.message;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * @author zhouhao
 */
public class WebSocketMessage implements Serializable {
    private int code;

    private String message;

    private Object data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public WebSocketMessage() {
    }

    public WebSocketMessage(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public WebSocketMessage(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
