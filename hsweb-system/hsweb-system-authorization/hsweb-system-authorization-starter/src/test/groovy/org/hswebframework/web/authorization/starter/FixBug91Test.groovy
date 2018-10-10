package org.hswebframework.web.authorization.starter

import org.hswebframework.web.authorization.AuthenticationManager
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest
import org.hswebframework.web.entity.authorization.UserEntity
import org.hswebframework.web.service.authorization.UserService
import org.hswebframework.web.validate.ValidationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Shared
import spock.lang.Specification

@WebAppConfiguration
@ContextConfiguration
@SpringBootTest(classes = [TestApplication.class], properties = ["classpath:application.yml"])
@Configuration
class FixBug91Test extends Specification {

    @Autowired
    private ConfigurableApplicationContext context;

    @Shared
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        UserEntity userEntity = userService.createEntity();
        userEntity.setName("test");
        userEntity.setUsername("fix-bug#91");
        userEntity.setPassword("fix-bug#91");
        if (userService.selectByUsername("fix-bug#91") == null) {
            userService.insert(userEntity);
        }
    }

    boolean authenticationInitSuccess(String username, String password) {
        try {
            def autz = authenticationManager.authenticate(new PlainTextUsernamePasswordAuthenticationRequest(username, password));
            if (autz != null) {
                return null != authenticationManager.getByUserId(autz.getUser().getId());
            }
        } catch (ValidationException e) {
            return false;
        }
        return false;
    }

    def "同时获取配置文件和数据库中的用户权限"() {
        expect:
        authenticationInitSuccess(username, password) == success
        where:
        username            | password            | success
        "fix-bug#91"        | "fix-bug#91"        | true
        "fix-bug-91-in-yml" | "fix-bug-91-in-yml" | true
        "not-exists-user"   | "not-exists-user"   | false
    }
}
