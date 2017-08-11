package org.hswebframework.web.socket.handler;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonParseException;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.socket.CommandRequest;
import org.hswebframework.web.socket.WebSocketSessionListener;
import org.hswebframework.web.socket.message.WebSocketMessage;
import org.hswebframework.web.socket.processor.CommandProcessor;
import org.hswebframework.web.socket.processor.CommandProcessorContainer;
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

    private CommandProcessorContainer processorContainer;

    private UserTokenManager userTokenManager;

    private List<WebSocketSessionListener> webSocketSessionListeners;

    public void setWebSocketSessionListeners(List<WebSocketSessionListener> webSocketSessionListeners) {
        this.webSocketSessionListeners = webSocketSessionListeners;
    }

    public void setUserTokenManager(UserTokenManager userTokenManager) {
        this.userTokenManager = userTokenManager;
    }

    public void setProcessorContainer(CommandProcessorContainer processorContainer) {
        this.processorContainer = processorContainer;
    }

    private static final TextMessage requestFormatErrorMessage = new TextMessage(new WebSocketMessage(400, "message format error!").toString());

    private static final TextMessage commandNotFoundMessage = new TextMessage(new WebSocketMessage(404, "command not found!").toString());

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if (StringUtils.isEmpty(payload)) return;
        try {
            WebSocketCommandRequest request = JSON.parseObject(payload, WebSocketCommandRequest.class);
            CommandRequest command = buildCommand(request, session);
            CommandProcessor processor = processorContainer.getProcessor(request.getCommand());
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
        if (null == userTokenManager) return null;
        return WebSocketUtils.getAuthentication(userTokenManager, socketSession);
    }

    private CommandRequest buildCommand(WebSocketCommandRequest request, WebSocketSession socketSession) {
        return new CommandRequest() {
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
