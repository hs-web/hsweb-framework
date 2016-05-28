package org.hsweb.web.core;

import org.hsweb.web.core.session.HttpSessionManager;
import org.hsweb.web.core.session.siample.SimpleHttpSessionManager;
import org.hsweb.web.core.session.siample.UserLoginOutListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
/**
 * Created by zhouhao on 16-5-6.
 */
@Configuration
@ComponentScan("org.hsweb.web.core")
public class CoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(HttpSessionManager.class)
    @ConditionalOnWebApplication
    public HttpSessionManager simpleHttpSessionManager() {
        SimpleHttpSessionManager httpSessionManager = new SimpleHttpSessionManager();
        return httpSessionManager;
    }

    @Bean
    @ConditionalOnMissingBean(HttpSessionManager.class)
    @ConditionalOnWebApplication
    public UserLoginOutListener sessionListener() {
        UserLoginOutListener loginOutListener = new UserLoginOutListener();
        loginOutListener.setHttpSessionManager(simpleHttpSessionManager());
        return loginOutListener;
    }

}
