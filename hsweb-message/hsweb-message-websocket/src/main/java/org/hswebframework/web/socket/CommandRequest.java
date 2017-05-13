package org.hswebframework.web.socket;

import org.hswebframework.web.authorization.Authentication;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface CommandRequest {
    Authentication getAuthentication();

    Map<String, Object> getParameters();

    WebSocketSession getSession();
}
