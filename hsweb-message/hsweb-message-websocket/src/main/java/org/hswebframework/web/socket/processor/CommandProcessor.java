package org.hswebframework.web.socket.processor;

import org.hswebframework.web.socket.CommandRequest;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface CommandProcessor {
    String getName();

    void execute(CommandRequest command);

    void init();

    void destroy();
}
