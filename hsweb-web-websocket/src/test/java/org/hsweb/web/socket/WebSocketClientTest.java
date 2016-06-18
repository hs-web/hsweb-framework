package org.hsweb.web.socket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

/**
 * 使用java模拟websocket客户端
 * Created by 浩 on 2016-01-20 0020.
 */
public class WebSocketClientTest {

    public static void main(String[] args) throws Exception {
        WebSocketClient client = new StandardWebSocketClient();
        String url = "ws://localhost:8080/socket";
        client.doHandshake(new AbstractWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                //链接成功后发送消息
                session.sendMessage(new TextMessage("{\"cmd\":\"system-monitor\",\"params\":{\"type\":\"cpu\"}}"));
            }

            @Override
            public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                System.out.println(message.getPayload());
            }
        }, url);
        System.in.read();
    }
}
