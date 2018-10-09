package org.hswebframework.web.authorization.starter;

import org.hswebframework.web.authorization.basic.web.AuthorizationController;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@SpringBootApplication
@WebAppConfiguration
@Configuration
public class TestApplication {

    @Bean
    public AuthorizationController authorizationController() {
        return new AuthorizationController();
    }


}
