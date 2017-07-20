package org.hswebframework.web.message.support.simple;

import org.hswebframework.web.message.support.TextMessage;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleTextMessage implements TextMessage {
    private String message;

    public SimpleTextMessage() {
    }

    public SimpleTextMessage(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.valueOf(message);
    }
}
