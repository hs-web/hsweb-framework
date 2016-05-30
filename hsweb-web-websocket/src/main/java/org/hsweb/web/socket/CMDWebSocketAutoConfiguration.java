package org.hsweb.web.socket;

import org.hsweb.web.socket.cmd.support.SystemMonitorProcessor;
import org.hsweb.web.socket.message.SimpleWebSocketMessageManager;
import org.hsweb.web.socket.message.WebSocketMessageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    @Autowired
    private CmdWebSocketHandler cmdWebSocketHandler;
    @Bean
    @ConditionalOnMissingBean(WebSocketMessageManager.class)
    public SimpleWebSocketMessageManager simpleWebSocketMessageManager() {
        return new SimpleWebSocketMessageManager();
    }

    @Bean
    @ConditionalOnClass(name = "org.hyperic.sigar.Sigar")
    public SystemMonitorProcessor systemMonitorProcessor() {
        SystemMonitorProcessor processor = new SystemMonitorProcessor();
        processor.setWebSocketMessageManager(simpleWebSocketMessageManager());
        return processor;
    }

    @Override
    protected void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        //绑定到 /socket
        registry.addHandler(cmdWebSocketHandler, "/socket");
        registry.addHandler(cmdWebSocketHandler, "/socket/js").withSockJS();
    }
}
