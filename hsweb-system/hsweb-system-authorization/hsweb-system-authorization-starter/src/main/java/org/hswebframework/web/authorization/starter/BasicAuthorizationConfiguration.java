package org.hswebframework.web.authorization.starter;

import org.hswebframework.web.authorization.basic.web.UserTokenForTypeParser;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.service.authorization.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0
 */
@Configuration
@ConditionalOnProperty(prefix = "hsweb.authorize", name = "basic-authorization", havingValue = "true")
@ConditionalOnClass(UserTokenForTypeParser.class)
public class BasicAuthorizationConfiguration {

    @Bean
    public BasicAuthorizationTokenParser basicAuthorizationTokenParser(UserService userService, UserTokenManager tokenManager) {
        return new BasicAuthorizationTokenParser(userService, tokenManager);
    }

}
