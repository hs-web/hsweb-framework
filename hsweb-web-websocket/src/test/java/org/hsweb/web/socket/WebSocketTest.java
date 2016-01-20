package org.hsweb.web.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * spring-boot websokcet 测试
 * Created by 浩 on 2016-01-19 0019.
 */
@ComponentScan(basePackages = "org.hsweb.web.socket")
@EnableAutoConfiguration
public class WebSocketTest extends WebSocketConfigurationSupport {

    /**
     * 基于命令的websocket服务
     */
    @Autowired
    private CmdWebSocketHandler cmdWebSocketHandler;

    /**
     * 注册WebSocket处理器
     */
    @Override
    protected void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //绑定到 /socket
        registry.addHandler(cmdWebSocketHandler, "/socket");
    }

    /**
     * 测试步骤
     * 1.运行成功后 请使用浏览器打开: http://localhost:8080
     * 2.忽略错误提示，按F12，进入console。
     * 3.执行:var ws = new WebSocket('ws://localhost:8080/socket');
     * 3.执行:ws.onmessage=function(message){console.log(message.data)}
     * 4.执行:ws.send('{"cmd":"test"}'); 按回车
     * 5.如果看到后台日志显示：handleMessage,id:0 msg={"cmd":"test"}，前台有接收到推送消息，则代表成功了
     * <p/>
     * 也可以运行：{@link WebSocketClientTest#main} 测试
     *
     * @throws Exception
     */
    public static void main(String[] args) {
        SpringApplication.run(WebSocketTest.class);
    }
}
