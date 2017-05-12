package org.hswebframework.web.socket.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhouhao
 */
public class DefaultWebSocketProcessorContainer implements WebSocketProcessorContainer {

    private final ConcurrentMap<String, WebSocketProcessor> processorStore = new ConcurrentHashMap<>();

    @Override
    public WebSocketProcessor install(WebSocketProcessor command) {
        command.init();
        return processorStore.put(command.getName(), command);
    }

    @Override
    public WebSocketProcessor uninstall(String name) {
        WebSocketProcessor processor = processorStore.remove(name);
        if (null != processor) processor.destroy();
        return processor;
    }

    public void destroy() {
        getAllProcessor().forEach(WebSocketProcessor::destroy);
        processorStore.clear();
    }

    @Override
    public WebSocketProcessor getProcessor(String name) {
        return processorStore.get(name);
    }

    @Override
    public List<WebSocketProcessor> getAllProcessor() {
        return new ArrayList<>(processorStore.values());
    }
}
