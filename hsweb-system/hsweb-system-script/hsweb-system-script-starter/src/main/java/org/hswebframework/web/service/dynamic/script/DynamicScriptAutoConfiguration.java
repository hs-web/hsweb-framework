package org.hswebframework.web.service.dynamic.script;

import org.hswebframework.web.service.script.simple.DefaultScriptExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0
 */
@Configuration
@ComponentScan({
        "org.hswebframework.web.service.script",
        "org.hswebframework.web.controller.script"
})
public class DynamicScriptAutoConfiguration {

    @Bean
    public DefaultScriptExecutorService defaultScriptExecutorService() {
        return new DefaultScriptExecutorService();
    }
}
