package org.hswebframework.web.socket.starter;

import org.hswebframework.web.authorization.container.AuthenticationContainer;
import org.hswebframework.web.message.Messager;
import org.hswebframework.web.socket.WebSocketSessionListener;
import org.hswebframework.web.socket.handler.CommandWebSocketMessageDispatcher;
import org.hswebframework.web.socket.message.DefaultWebSocketMessager;
import org.hswebframework.web.socket.message.WebSocketMessager;
import org.hswebframework.web.socket.processor.DefaultWebSocketProcessorContainer;
import org.hswebframework.web.socket.processor.WebSocketProcessor;
import org.hswebframework.web.socket.processor.WebSocketProcessorContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
public class CommandWebSocketAutoConfiguration {

    @Configuration
    @ConditionalOnMissingBean(WebSocketProcessorContainer.class)
    public static class WebSocketProcessorContainerConfiguration {
        @Autowired(required = false)
        private List<WebSocketProcessor> webSocketProcessors;

        @Bean(destroyMethod = "destroy")
        public DefaultWebSocketProcessorContainer defaultWebSocketProcessorContainer() {
            DefaultWebSocketProcessorContainer container = new DefaultWebSocketProcessorContainer();
            if (webSocketProcessors != null) {
                webSocketProcessors.forEach(container::install);
            }
            return container;
        }
    }

    @Configuration
    @ConditionalOnBean(Messager.class)
    @ConditionalOnMissingBean(WebSocketMessager.class)
    public static class WebSocketMessagerConfiguration {
        @Bean
        public WebSocketMessager webSocketMessager(Messager messager) {
            return new DefaultWebSocketMessager(messager);
        }
    }

    @Configuration
    public static class HandlerConfigruation extends WebSocketConfigurationSupport {
        @Autowired(required = false)
        private AuthenticationContainer authenticationContainer;

        @Autowired(required = false)
        private List<WebSocketSessionListener> webSocketSessionListeners;

        @Autowired
        private WebSocketProcessorContainer webSocketProcessorContainer;

        @Override
        protected void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            CommandWebSocketMessageDispatcher dispatcher = new CommandWebSocketMessageDispatcher();
            dispatcher.setProcessorContainer(webSocketProcessorContainer);
            dispatcher.setAuthenticationContainer(authenticationContainer);
            dispatcher.setWebSocketSessionListeners(webSocketSessionListeners);
            registry.addHandler(dispatcher, "/sockjs")
                    .withSockJS()
                    .setSessionCookieNeeded(true);
            registry.addHandler(dispatcher, "/socket");
        }
    }

}
