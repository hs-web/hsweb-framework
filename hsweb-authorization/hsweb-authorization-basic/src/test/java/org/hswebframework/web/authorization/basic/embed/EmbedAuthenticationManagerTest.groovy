package org.hswebframework.web.authorization.basic.embed

import org.hswebframework.web.authorization.Authentication
import org.hswebframework.web.authorization.AuthenticationManager
import org.hswebframework.web.authorization.TestApplication
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import spock.lang.Specification

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@WebAppConfiguration
@ContextConfiguration
@SpringBootTest(classes = [TestApplication.class], properties = ["classpath:application.yml"])
class EmbedAuthenticationManagerTest extends Specification {

    @Autowired
    private AuthenticationManager manager;


    def "Test"() {
        setup:
        Authentication authentication = manager.authenticate(new PlainTextUsernamePasswordAuthenticationRequest("admin", "admin"));
        expect:
        authentication != null
        authentication.getUser() != null
        authentication.getUser().getName() == "超级管理员"
        authentication.hasPermission("user-manager", "query")
        authentication.hasPermission("test", "query")
        authentication.getPermission("user-manager") != null
        authentication.hasRole("user")
        authentication.getPermission("user-manager")
                .get().findDenyFields("query") != null
        authentication.getPermission("user-manager")
                .get().findDenyFields("query").contains("password")
    }
}
