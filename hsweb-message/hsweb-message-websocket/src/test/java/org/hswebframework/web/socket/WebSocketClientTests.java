package org.hswebframework.web.socket;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

public class WebSocketClientTests {
    public static void main(String[] args) throws Exception {
//        for (int i = 0; i < 10; i++) {
            WebSocketClient client = new StandardWebSocketClient();
            String url = "ws://localhost:8081/socket";
            ListenableFuture<WebSocketSession> future = client.doHandshake(new AbstractWebSocketHandler() {
                @Override
                public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                    System.out.println(message.getPayload());
                }
            }, url);

            WebSocketSession socketSession = future.get();
            socketSession.sendMessage(new TextMessage("{\"command\":\"test\",\"parameters\":{\"type\":\"conn\"}}"));
//        }
        System.in.read();
    }
}
