package org.hswebframework.web.socket.handler;

import java.util.Map;

/**
 * @author zhouhao
 */
public class WebSocketCommandRequest {
    private String command;

    private Map<String, Object> parameters;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
