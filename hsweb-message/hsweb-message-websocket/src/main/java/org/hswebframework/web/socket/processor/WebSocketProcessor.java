package org.hswebframework.web.socket.processor;

import org.hswebframework.web.socket.WebSocketCommand;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface WebSocketProcessor {
    String getName();

    void execute(WebSocketCommand command);

    void init();

    void destroy();
}
