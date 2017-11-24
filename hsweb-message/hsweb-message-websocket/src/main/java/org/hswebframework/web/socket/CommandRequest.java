package org.hswebframework.web.socket;

import org.hswebframework.web.authorization.Authentication;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * @author zhouhao
 */
public interface CommandRequest {
    Authentication getAuthentication();

    Map<String, Object> getParameters();

    WebSocketSession getSession();
}
