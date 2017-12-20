package org.hswebframework.web.authorization.cloud;

import org.hswebframework.web.authorization.cloud.server.UserTokenController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0
 */
@Configuration
public class AuthorizationServerAutoConfiguration {

    @Bean
    public UserTokenController userTokenController() {
        return new UserTokenController();
    }
}
