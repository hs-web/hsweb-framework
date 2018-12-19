package org.hswebframework.web.authorization.full

import com.alibaba.fastjson.JSON
import org.hswebframework.web.authorization.AuthenticationManager
import org.hswebframework.web.authorization.TestApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Shared
import spock.lang.Specification
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 完整功能测试
 * @author zhouhao
 * @since 3.0.2
 */
@WebAppConfiguration
@ContextConfiguration
@SpringBootTest(classes = [TestApplication.class], properties = ["classpath:application.yml"])
class FullFunctionTest extends Specification {

    @Autowired
    private ConfigurableApplicationContext context;

    @Shared
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationManager authenticationManager;

    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    def doLogin(String username, String password) {
        return JSON.parseObject(mockMvc.perform(post("/authorize/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"token_type":"test-token","username":"${username}","password":"${password}"}"""))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString())
                .getJSONObject("result").getString("token")
    }


    def "测试双重验证"() {
        given: "登录"
        def token = doLogin("admin", "admin")
        when: "登录成功"
        token != null
        then: "调用双重验证接口"
        mockMvc.perform(get("/test/two-factor")
                .header("token", token))
                .andExpect(status().is(403))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def resp = mockMvc.perform(get("/test/two-factor")
                .header("token", token)
                .param("verifyCode", "test"))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        expect:
        resp != null
    }

    def "测试查询"() {
        given: "登录"
        def token = doLogin("admin", "admin")
        when: "登录成功"
        token != null
        then: "进行查询"
        def resp = mockMvc.perform(get("/test")
                .header("token", token)//登录返回的token
                .param("terms[0].column", "name")
                .param("terms[0].value", "test"))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def result = JSON.parseObject(resp).getJSONObject("result");
        expect: "权限控制成功"
        result.getJSONArray("excludes") != null
        //与application.yml中配置的数据权限一致
        result.getJSONArray("excludes").getString(0) == "password"
        result.getJSONArray("terms") != null
        !result.getJSONArray("terms").isEmpty()
    }

    def "测试修改"() {
        given: "登录"
        def token = doLogin("admin", "admin")
        when: "登录成功"
        token != null
        then: "进行修改数据"
        def resp = mockMvc.perform(put("/test")
                .header("token", token)//登录返回的token
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"id":"test","name":"testName"}"""))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def result = JSON.parseObject(resp).getJSONObject("result");
        println result
        expect: "权限控制成功,name属性被修改为null"
        //与application.yml中配置的数据权限一致
        result.getString("name") == null
        result.getString("id") != null
    }

    def "测试新增"() {
        given: "登录"
        def token = doLogin("admin", "admin")
        when: "登录成功"
        token != null
        then: "进行新增数据"
        def resp = mockMvc.perform(post("/test")
                .header("token", token)//登录返回的token
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"id":"test","name":"testName"}"""))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def result = JSON.parseObject(resp).getJSONObject("result");
        expect: "权限控制成功,id不能进行insert操作"
        //与application.yml中配置的数据权限一致
        result.getString("id") == null
        result.getString("name") != null
    }

    def "测试删除"() {
        given: "登录"
        def token = doLogin("admin", "admin")
        when: "登录成功"
        token != null
        then: "进行新增数据"
        def resp = mockMvc.perform(delete("/test/{id}", "test")
                .header("token", token))//登录返回的token
                .andReturn()
                .getResponse()
                .getContentAsString()
        def status = JSON.parseObject(resp).getInteger("status");
        expect:
        "权限控制成功,不能进行delete操作"
        //与application.yml中配置的数据权限一致
        status == 403
    }
}
