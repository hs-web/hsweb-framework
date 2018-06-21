package org.hswebframework.web.workflow.flowable

import com.alibaba.fastjson.JSON
import org.hswebframework.web.authorization.AuthenticationInitializeService
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author zhouhao
 * @since
 */
@WebAppConfiguration
@ContextConfiguration
@SpringBootTest(classes = [TestApplication.class], properties = ["classpath:application.yml"])
class WorkFlowModelTests extends Specification {
    @Autowired
    private ConfigurableApplicationContext context;

    @Shared
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationInitializeService initializeService;

    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    def "Add Model"() {
        setup:
        //添加模型
        def json = mockMvc.perform(post("/workflow/model/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "key":"testModel",
                            "name":"测试模型"
                        }
                        """))
                .andExpect(status().is(201))
                .andReturn().getResponse().getContentAsString();
        println(json)

        and:
        def jsonObject = JSON.parseObject(json)
        def id = jsonObject.getJSONObject("result").getString("id");

        expect:
        jsonObject != null
        id != null
    }

}
