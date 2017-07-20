package org.hswebframework.web.socket;

import org.hswebframework.web.socket.message.WebSocketMessage;
import org.hswebframework.web.socket.message.WebSocketMessager;
import org.hswebframework.web.socket.processor.CommandProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class TestProcessor implements CommandProcessor, WebSocketSessionListener {

    @Autowired
    private WebSocketMessager messager;

    @Override
    public String getName() {
        return "test";
    }

    private void sub(WebSocketSession socketSession) {
        messager.subscribeQueue(getName(), socketSession);
    }

    private void deSub(WebSocketSession socketSession) {
        messager.deSubscribeQueue(getName(), socketSession);
    }

    @Override
    public void execute(CommandRequest command) {
        String type = String.valueOf(command.getParameters().get("type"));
        switch (type) {
            case "conn":
                sub(command.getSession());
                break;
            case "close": {
                deSub(command.getSession());
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
                if (messager.getSubscribeTotal(getName(), WebSocketMessager.TYPE_QUEUE) > 0) {
                    messager.publishQueue(getName(), new WebSocketMessage(200, "hello" + total++));
                    System.out.println(total);
                }
            }
        }).start();
    }

    @Override
    public void destroy() {
    }

    @Override
    public void onSessionConnect(WebSocketSession session) {

    }

    @Override
    public void onSessionClose(WebSocketSession session) {
        deSub(session);
    }
}
