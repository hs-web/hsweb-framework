package org.hsweb.web.message;

/**
 * @author zhouhao
 */
public interface MessageManager {

    void send(Message message, boolean discardIfUserOffline);

}
