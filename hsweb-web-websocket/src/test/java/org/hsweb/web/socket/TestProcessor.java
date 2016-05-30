package org.hsweb.web.socket;

import org.hsweb.web.socket.cmd.CMD;
import org.hsweb.web.socket.cmd.CmdProcessor;
import org.hsweb.web.socket.message.SimpleWebSocketMessageManager;
import org.hsweb.web.socket.message.WebSocketMessage;
import org.hsweb.web.socket.message.WebSocketMessageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * 测试命令处理器
 * Created by 浩 on 2016-01-19 0019.
 */
@Component
public class TestProcessor implements CmdProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private WebSocketMessageManager webSocketMessageManager = new SimpleWebSocketMessageManager();

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public void exec(CMD cmd) throws Exception {
        logger.info("execute cmd :" + cmd);
        //收到命令后，向客户端推送一条消息
        if ("subscribe".equals(cmd.getParams().get("type"))) {
            webSocketMessageManager.subscribe("test", "admin",cmd.getSession());
        }
        WebSocketMessage message = new WebSocketMessage();
        message.setType("test");
        message.setTo("admin");
        message.setContent("test");
        new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                try {
                    Thread.sleep(2000);
                    webSocketMessageManager.publish(message);
                } catch (Exception e) {
                }
            }
        }).start();
    }

    @Override
    @PostConstruct
    public void init() throws Exception {
        logger.info("init TestProcessor");
    }

    @Override
    public void onSessionConnect(WebSocketSession session) throws Exception {
        logger.info("小伙伴进来了");
       // webSocketMessageManager.onSessionConnect(session);
    }

    @Override
    public void onSessionClose(WebSocketSession session) throws Exception {
        logger.info("小伙伴离开了");
       // webSocketMessageManager.onSessionClose(session);
    }
}
