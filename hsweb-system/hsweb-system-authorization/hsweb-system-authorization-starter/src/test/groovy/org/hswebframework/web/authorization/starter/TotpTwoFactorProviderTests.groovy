package org.hswebframework.web.authorization.starter

import org.hswebframework.web.authorization.twofactor.TwoFactorValidatorManager
import org.hswebframework.web.entity.authorization.SimpleUserEntity
import org.hswebframework.web.service.authorization.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
 * @author zhouhao
 * @since 3.0.4
 */
@ContextConfiguration
@SpringBootTest(classes = [TestApplication.class], properties = ["classpath:application.yml"])
class TotpTwoFactorProviderTests extends Specification {

    @Autowired
    TwoFactorValidatorManager validatorManager;

    @Autowired
    private UserService userService;


    def "测试totp"() {
        given:
        String id = userService.insert(new SimpleUserEntity(
                username: "admin2",
                password: "admin2",
                name: "admin2"
        ))
        expect:
        !validatorManager.getValidator(id, "", "totp")
                .verify("test", 100)
    }
}
