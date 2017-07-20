package org.hswebframework.web.socket.processor;


import java.util.List;

/**
 * @author zhouhao
 */
public interface CommandProcessorContainer {
    CommandProcessor install(CommandProcessor command);

    CommandProcessor uninstall(String name);

    CommandProcessor getProcessor(String name);

    List<CommandProcessor> getAllProcessor();
}
