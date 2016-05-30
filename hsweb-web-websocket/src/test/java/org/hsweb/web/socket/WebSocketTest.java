package org.hsweb.web.socket;

import org.hsweb.web.socket.cmd.support.SystemMonitorProcessor;
import org.hsweb.web.socket.message.SimpleWebSocketMessageManager;
import org.hsweb.web.socket.message.WebSocketMessageManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

/**
 * spring-boot websokcet 测试
 * Created by 浩 on 2016-01-19 0019.
 */
@Configuration
@EnableAutoConfiguration
public class WebSocketTest {

    /**
     * 测试步骤
     * 1.运行成功后 请使用浏览器打开: http://localhost:8080
     * 2.忽略错误提示，按F12，进入console。
     * 3.执行:var ws = new WebSocket('ws://localhost:8080/socket');
     * 3.执行:ws.onmessage=function(message){console.log(message.data)}
     * 4.执行:ws.send('{"cmd":"test"}'); 按回车
     * 5.如果看到后台日志显示：handleMessage,id:0 msg={"cmd":"test"}，前台有接收到推送消息，则代表成功了
     * <p>
     * 也可以运行：{@link WebSocketClientTest#main} 测试
     *
     * @throws Exception
     */
    public static void main(String[] args) {
        SpringApplication.run(WebSocketTest.class);
    }
}
