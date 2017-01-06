package org.hswebframework.web.message;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface MessageManager {

    void send(String toUser,String destination, Message message);

    void publish(String topic, Message message);

    <T extends Message> void subscribe(String topic, MessageListener<T> listener);

    void deSubscribe(String topic);
}
