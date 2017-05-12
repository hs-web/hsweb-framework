package org.hswebframework.web.socket;

import org.hswebframework.web.message.MessageSubscribe;
import org.hswebframework.web.message.Messager;
import org.hswebframework.web.message.builder.StaticMessageBuilder;
import org.hswebframework.web.message.builder.StaticMessageSubjectBuilder;
import org.hswebframework.web.message.support.ObjectMessage;
import org.hswebframework.web.socket.message.WebSocketMessage;
import org.hswebframework.web.socket.message.WebSocketMessager;
import org.hswebframework.web.socket.processor.WebSocketProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hswebframework.web.message.builder.StaticMessageBuilder.object;
import static org.hswebframework.web.message.builder.StaticMessageSubjectBuilder.topic;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class TestProcessor implements WebSocketProcessor, WebSocketSessionListener {

    @Autowired
    private Messager messager;
    Map<String, MessageSubscribe<ObjectMessage<WebSocketMessage>>>
            store = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "test";
    }

    public void sub(WebSocketSession socketSession) {
        MessageSubscribe<ObjectMessage<WebSocketMessage>> subscribe =
                store.get(socketSession.getId());
        if (subscribe != null) return;
        store.put(socketSession.getId(), messager.<ObjectMessage<WebSocketMessage>>subscribe(topic("test"))
                .onMessage(message -> {
                    try {
                        if (!socketSession.isOpen()) {
                            desub(socketSession);
                            return;
                        }
                        socketSession.sendMessage(new TextMessage(message.getObject().toString()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
    }

    public void desub(WebSocketSession socketSession) {
        MessageSubscribe<ObjectMessage<WebSocketMessage>> subscribe =
                store.get(socketSession.getId());
        if (subscribe == null) return;
        subscribe.cancel();
        store.remove(socketSession.getId());
    }

    @Override
    public void execute(WebSocketCommand command) {
        String type = String.valueOf(command.getParameters().get("type"));
        switch (type) {
            case "conn":
                sub(command.getSession());
                break;
            case "close": {
                desub(command.getSession());
            }
        }
    }

    @Override
    public void init() {
        new Thread(() -> {
            long total = 0;
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                messager.publish(object(new WebSocketMessage(200, "hello" + total++)))
                        .to(topic("test"))
                        .send();
                System.out.println(total);
            }
        }).start();
    }

    @Override
    public void destroy() {
        store.values().forEach(MessageSubscribe::cancel);
        store.clear();
    }

    @Override
    public void onSessionConnect(WebSocketSession session) {

    }

    @Override
    public void onSessionClose(WebSocketSession session) {
        desub(session);
    }
}
