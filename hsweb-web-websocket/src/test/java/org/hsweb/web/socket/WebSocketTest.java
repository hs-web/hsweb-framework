package org.hsweb.web.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Created by 浩 on 2016-01-19 0019.
 */
@Configuration
@ComponentScan(basePackages = "org.hsweb.web.socket")
@EnableAutoConfiguration
public class WebSocketTest extends WebSocketConfigurationSupport {

    @Autowired
    private CmdWebSocketHandler cmdWebSocketHandler;

    @Override
    protected void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(cmdWebSocketHandler, "/socket");
    }

    public static void main(String[] args) {
        SpringApplication.run(WebSocketTest.class);
        //运行成功后 请使用浏览器打开: http://localhost:8080
        //忽略错误提示，按F12，进入console。
        //运行:var ws = new WebSocket('ws://localhost:8080/socket');
        //运行:ws.send('{"cmd":"test"}'); 按回车
        //如果看到后台日志显示 handleMessage,id:0 msg={"cmd":"test"} 则代表成功了
    }
}
