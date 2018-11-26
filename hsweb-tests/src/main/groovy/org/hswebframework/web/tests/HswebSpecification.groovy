package org.hswebframework.web.tests

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author zhouhao
 * @since 3.0.2
 */
@WebAppConfiguration
@ContextConfiguration
@SpringBootTest(classes = [HswebTestApplication.class], properties = ["classpath:application.yml"])
class HswebSpecification extends Specification {
    @Autowired
    protected WebApplicationContext context;

    @Shared
    protected MockMvc mockMvc;

    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    MockMvc getMock() {
        return mockMvc;
    }

}
