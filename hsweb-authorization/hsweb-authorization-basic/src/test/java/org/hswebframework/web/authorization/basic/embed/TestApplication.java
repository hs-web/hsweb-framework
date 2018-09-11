package org.hswebframework.web.authorization.basic.embed;

import org.hswebframework.web.authorization.basic.configuration.AopAuthorizeAutoConfiguration;
import org.hswebframework.web.authorization.basic.configuration.AuthorizingHandlerAutoConfiguration;
import org.hswebframework.web.authorization.basic.configuration.EnableAopAuthorize;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@SpringBootApplication
@WebAppConfiguration
@EnableAopAuthorize
public class TestApplication {

}
