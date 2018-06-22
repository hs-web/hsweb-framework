package org.hswebframework.web.service.authorization.simple.terms;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Configuration
public class CustomUserSqlTermAutoConfiguration {

    @Bean
    public UserInRoleSqlTerm userInRoleSqlTerm() {
        return new UserInRoleSqlTerm(false);
    }

    @Bean
    public UserInRoleSqlTerm userNotInRoleSqlTerm() {
        return new UserInRoleSqlTerm(true);
    }
}
