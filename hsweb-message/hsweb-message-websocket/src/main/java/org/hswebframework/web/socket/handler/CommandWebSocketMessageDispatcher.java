package org.hswebframework.web.socket.handler;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonParseException;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.container.AuthenticationContainer;
import org.hswebframework.web.socket.WebSocketCommand;
import org.hswebframework.web.socket.WebSocketSessionListener;
import org.hswebframework.web.socket.message.WebSocketMessage;
import org.hswebframework.web.socket.processor.WebSocketProcessor;
import org.hswebframework.web.socket.processor.WebSocketProcessorContainer;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;

/**
 * @author zhouhao
 */
public class CommandWebSocketMessageDispatcher extends TextWebSocketHandler {

    private WebSocketProcessorContainer processorContainer;

    private AuthenticationContainer authenticationContainer;

    private List<WebSocketSessionListener> webSocketSessionListeners;

    public void setWebSocketSessionListeners(List<WebSocketSessionListener> webSocketSessionListeners) {
        this.webSocketSessionListeners = webSocketSessionListeners;
    }

    public void setAuthenticationContainer(AuthenticationContainer authenticationContainer) {
        this.authenticationContainer = authenticationContainer;
    }

    public void setProcessorContainer(WebSocketProcessorContainer processorContainer) {
        this.processorContainer = processorContainer;
    }

    private static final TextMessage requestFormatErrorMessage = new TextMessage(new WebSocketMessage(400, "message format error!").toString());

    private static final TextMessage commandNotFoundMessage = new TextMessage(new WebSocketMessage(404, "command not found!").toString());

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if (StringUtils.isEmpty(payload)) return;
        try {
            CommandRequest request = JSON.parseObject(payload, CommandRequest.class);
            WebSocketCommand command = buildCommand(request, session);
            WebSocketProcessor processor = processorContainer.getProcessor(command.getCommand());
            if (processor != null) {
                processor.execute(command);
            } else {
                session.sendMessage(commandNotFoundMessage);
            }
        } catch (JsonParseException e) {
            session.sendMessage(requestFormatErrorMessage);
        } catch (Exception e) {
            e.printStackTrace();
            session.sendMessage(new TextMessage(new WebSocketMessage(500, "error!" + e.getMessage()).toString()));
        }
    }

    private Authentication getAuthenticationFromSession(WebSocketSession socketSession) {
        if (null == authenticationContainer) return null;
        return WebSocketUtils.getAuthentication(authenticationContainer, socketSession);
    }

    private WebSocketCommand buildCommand(CommandRequest request, WebSocketSession socketSession) {
        return new WebSocketCommand() {
            @Override
            public String getCommand() {
                return request.getCommand();
            }

            @Override
            public Authentication getAuthentication() {
                return getAuthenticationFromSession(socketSession);
            }

            @Override
            public Map<String, Object> getParameters() {
                return request.getParameters();
            }

            @Override
            public WebSocketSession getSession() {
                return socketSession;
            }
        };
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (webSocketSessionListeners != null) webSocketSessionListeners.forEach(webSocketSessionListener ->
                webSocketSessionListener.onSessionConnect(session));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if (webSocketSessionListeners != null) webSocketSessionListeners.forEach(webSocketSessionListener ->
                webSocketSessionListener.onSessionClose(session));
    }
}
