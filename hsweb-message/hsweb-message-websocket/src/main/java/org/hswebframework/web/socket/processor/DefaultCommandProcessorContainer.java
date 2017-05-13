package org.hswebframework.web.socket.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author zhouhao
 */
public class DefaultCommandProcessorContainer implements CommandProcessorContainer {

    private final ConcurrentMap<String, CommandProcessor> processorStore = new ConcurrentHashMap<>();

    @Override
    public CommandProcessor install(CommandProcessor command) {
        command.init();
        return processorStore.put(command.getName(), command);
    }

    @Override
    public CommandProcessor uninstall(String name) {
        CommandProcessor processor = processorStore.remove(name);
        if (null != processor) processor.destroy();
        return processor;
    }

    public void destroy() {
        getAllProcessor().forEach(CommandProcessor::destroy);
        processorStore.clear();
    }

    @Override
    public CommandProcessor getProcessor(String name) {
        return processorStore.get(name);
    }

    @Override
    public List<CommandProcessor> getAllProcessor() {
        return new ArrayList<>(processorStore.values());
    }
}
