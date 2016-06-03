package org.hsweb.web.socket;

import org.hsweb.web.socket.cmd.support.SystemMonitorProcessor;
import org.hsweb.web.socket.message.SimpleWebSocketMessageManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Created by zhouhao on 16-5-29.
 */
@ComponentScan(basePackages = "org.hsweb.web.socket")
@Configuration
public class CMDWebSocketAutoConfiguration extends WebSocketConfigurationSupport {

    @Bean
    public SimpleWebSocketMessageManager simpleWebSocketMessageManager() {
        return new SimpleWebSocketMessageManager();
    }

    @Bean
    public CmdWebSocketHandler cmdWebSocketHandler() {
        CmdWebSocketHandler cmdWebSocketHandler = new CmdWebSocketHandler();
        cmdWebSocketHandler.setWebSocketMessageManager(simpleWebSocketMessageManager());
        return cmdWebSocketHandler;
    }

    @Bean
    @ConditionalOnClass(name = "org.hyperic.sigar.Sigar")
    public SystemMonitorProcessor systemMonitorProcessor() {
        return new SystemMonitorProcessor();
    }

    @Override
    protected void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //绑定到 /socket
        registry.addHandler(cmdWebSocketHandler(), "/socket");
        registry.addHandler(cmdWebSocketHandler(), "/socket/js").withSockJS();
    }
}
