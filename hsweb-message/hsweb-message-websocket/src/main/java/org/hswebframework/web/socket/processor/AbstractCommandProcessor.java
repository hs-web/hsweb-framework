package org.hswebframework.web.socket.processor;

import org.hswebframework.web.socket.message.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public abstract class AbstractCommandProcessor implements CommandProcessor {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());


    protected void sendMessage(WebSocketSession session, WebSocketMessage message) {
        message.setCommand(getName());
        try {
            session.sendMessage(new TextMessage(message.toString()));
        } catch (IOException e) {
            logger.error("send websocket message to {} error", session.getId(), message.toString(), e);
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }
}
