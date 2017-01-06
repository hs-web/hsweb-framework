package org.hswebframework.web.message;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface MessageListener<T extends Message> {
    void onMessage(T message);
}
