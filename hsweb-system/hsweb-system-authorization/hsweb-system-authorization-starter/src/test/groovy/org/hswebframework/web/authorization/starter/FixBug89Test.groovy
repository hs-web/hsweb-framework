package org.hswebframework.web.authorization.starter

import com.alibaba.fastjson.JSON
import org.hswebframework.web.entity.authorization.UserEntity
import org.hswebframework.web.service.authorization.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Shared
import spock.lang.Specification
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*


@WebAppConfiguration
@ContextConfiguration
@SpringBootTest(classes = [TestApplication.class], properties = ["classpath:application.yml"])
@Configuration
class FixBug89Test extends Specification {

    @Autowired
    private ConfigurableApplicationContext context;

    @Shared
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;


    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        UserEntity userEntity = userService.createEntity();
        userEntity.setName("test");
        userEntity.setUsername("fix-bug#89");
        userEntity.setPassword("fix-bug#89");
        if (userService.selectByUsername("fix-bug#89") == null) {
            userService.insert(userEntity);
        }
    }

    def doLogin(username, password) {
        def response = mockMvc.perform(post("/authorize/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"username":"${username}","password":"${password}"}"""))
//                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        return JSON.parseObject(response).get("status");
    }

    def "测试用户名为空时登录依旧能登录成功问题"() {
        given:
        def user = userService.selectByUserNameAndPassword("fix-bug#89", "fix-bug#89");
        expect:
        user != null
        doLogin(username, password) == code
        where:
        username     | password     | code
        "fix-bug#89" | "fix-bug#89" | 200
        "fix-bug#89" | ""           | 400
        ""           | "fix-bug#89" | 400
        ""           | ""           | 400


    }
}
