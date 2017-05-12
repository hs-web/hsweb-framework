package org.hswebframework.web.socket.processor;


import java.util.List;

/**
 * @author zhouhao
 */
public interface WebSocketProcessorContainer {
    WebSocketProcessor install(WebSocketProcessor command);

    WebSocketProcessor uninstall(String name);

    WebSocketProcessor getProcessor(String name);

    List<WebSocketProcessor> getAllProcessor();
}
