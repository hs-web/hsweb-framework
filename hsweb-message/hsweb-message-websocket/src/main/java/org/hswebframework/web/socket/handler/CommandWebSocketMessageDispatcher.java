package org.hswebframework.web.socket.handler;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonParseException;
import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationHolder;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenHolder;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.socket.CommandRequest;
import org.hswebframework.web.socket.WebSocketSessionListener;
import org.hswebframework.web.socket.authorize.WebSocketTokenParser;
import org.hswebframework.web.socket.message.WebSocketMessage;
import org.hswebframework.web.socket.processor.CommandProcessor;
import org.hswebframework.web.socket.processor.CommandProcessorContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhouhao
 */
public class CommandWebSocketMessageDispatcher extends TextWebSocketHandler {

    private CommandProcessorContainer processorContainer;

    private UserTokenManager userTokenManager;

    private List<WebSocketSessionListener> webSocketSessionListeners;

    private List<WebSocketTokenParser> tokenParsers;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void setWebSocketSessionListeners(List<WebSocketSessionListener> webSocketSessionListeners) {
        this.webSocketSessionListeners = webSocketSessionListeners;
    }

    public void setTokenParsers(List<WebSocketTokenParser> tokenParsers) {
        this.tokenParsers = tokenParsers;
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
        if (StringUtils.isEmpty(payload)) {
            return;
        }
        String cmd = null;
        WebSocketMessage errorMessage = null;
        try {
            WebSocketCommandRequest request = JSON.parseObject(payload, WebSocketCommandRequest.class);
            cmd = request.getCommand();
            CommandRequest command = buildCommand(request, session);
            CommandProcessor processor = processorContainer.getProcessor(request.getCommand());
            if (processor != null) {
                processor.execute(command);
            } else {
                session.sendMessage(commandNotFoundMessage);
            }
        } catch (JsonParseException e) {
            session.sendMessage(requestFormatErrorMessage);
        } catch (UnAuthorizedException e) {
            errorMessage = new WebSocketMessage(401, "un authorized");
        } catch (AccessDenyException e) {
            errorMessage = new WebSocketMessage(403, "access deny");
        } catch (Exception e) {
            logger.warn("handle websocket message error ", e);
            errorMessage = new WebSocketMessage(500, e.getMessage());
        } finally {
            ThreadLocalUtils.clear();
        }
        if (errorMessage != null) {
            errorMessage.setCommand(cmd);
            session.sendMessage(new TextMessage(errorMessage.toString()));
        }
    }

    private Authentication getAuthenticationFromSession(WebSocketSession session) {
        if (null == userTokenManager) {
            return null;
        }
        String token = (String) session.getAttributes().get("user_token");
        if(null==token){
            return null;
        }
        UserToken userToken = userTokenManager.getByToken(token);
        if (null == userToken) {
            return null;
        }
        UserTokenHolder.setCurrent(userToken);
        return Authentication.current().orElse(null);
    }

    private CommandRequest buildCommand(WebSocketCommandRequest request, WebSocketSession socketSession) {
        Authentication authentication = getAuthenticationFromSession(socketSession);
        return new CommandRequest() {
            @Override
            public Authentication getAuthentication() {
                return authentication;
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
        logger.debug("new WebSocket Session Established,sessionId:{}",session.getId());
        if (tokenParsers != null) {
            String token = tokenParsers.stream()
                    .map(parser -> parser.parseToken(session))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            if (null != token) {
                UserToken userToken = userTokenManager.getByToken(token);
                if (null != userToken) {
                    UserTokenHolder.setCurrent(userToken);
                    Authentication authentication = Authentication.current().orElse(null);
                    session.getAttributes().put("user_token", token);

                    if (null != authentication) {
                        logger.debug("websocket authentication init ok!");
                    } else {
                        logger.debug("websocket authentication init fail!");
                    }
                }
            }
        }
        if (webSocketSessionListeners != null) {
            webSocketSessionListeners.forEach(webSocketSessionListener ->
                    webSocketSessionListener.onSessionConnect(session));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.debug("WebSocket Session Closed,sessionId:{}",session.getId());

        ThreadLocalUtils.clear();
        if (webSocketSessionListeners != null) {
            webSocketSessionListeners.forEach(webSocketSessionListener ->
                    webSocketSessionListener.onSessionClose(session));
        }
    }
}
