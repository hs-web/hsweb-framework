package org.hsweb.web.core.session.simple;

import org.hsweb.web.core.session.HttpSessionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(HttpSessionManager.class)
@ConditionalOnWebApplication
public class SimpleHttpSessionManagerAutoConfiguration {

    @Bean(name = "httpSessionManager")
    public HttpSessionManager httpSessionManager() {
        SimpleHttpSessionManager httpSessionManager = new SimpleHttpSessionManager();
        return httpSessionManager;
    }

    @Bean
    public UserLoginOutListener sessionListener(HttpSessionManager httpSessionManager) {
        UserLoginOutListener loginOutListener = new UserLoginOutListener();
        loginOutListener.setHttpSessionManager(httpSessionManager);
        return loginOutListener;
    }
}