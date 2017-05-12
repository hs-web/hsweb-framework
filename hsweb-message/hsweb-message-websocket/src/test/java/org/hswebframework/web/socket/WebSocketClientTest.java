package org.hswebframework.web.socket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

public class WebSocketClientTest {

    public static void main(String[] args) throws Exception {
        WebSocketClient client = new StandardWebSocketClient();
        String url = "ws://localhost:8080/socket";
        client.doHandshake(new AbstractWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                //链接成功后发送消息
                System.out.println("发送消息");
                session.sendMessage(new TextMessage("{\"command\":\"test\",\"parameters\":{\"type\":\"conn\"}}"));
            }

            @Override
            public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                System.out.println(message.getPayload());
            }
        }, url);
        System.in.read();
    }
}
