package org.hswebframework.web.workflow.flowable

import com.alibaba.fastjson.JSON
import org.activiti.engine.RuntimeService
import org.activiti.engine.TaskService
import org.activiti.engine.impl.persistence.entity.ExecutionEntity
import org.activiti.engine.runtime.ProcessInstance
import org.hswebframework.web.authorization.AuthenticationInitializeService
import org.hswebframework.web.workflow.service.BpmProcessService
import org.hswebframework.web.workflow.service.BpmTaskService
import org.hswebframework.web.workflow.service.request.CompleteTaskRequest
import org.hswebframework.web.workflow.service.request.StartProcessRequest
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
class WorkFlowProcessTests extends Specification {
    @Autowired
    private ConfigurableApplicationContext context;

    @Shared
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationInitializeService initializeService;

    @Autowired
    private BpmProcessService bpmProcessService;

    @Autowired
    private BpmTaskService bpmTaskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    def "Start Process"() {
        setup:
        def request = new StartProcessRequest();
        request.setCreatorId("admin");
        request.setCreatorName("admin");
        request.setProcessDefineId("testid:1:4");
        request.setNextClaimUserId("admin")
        and:
        def instance = (ExecutionEntity) bpmProcessService.startProcessInstance(request);
        expect:
        instance != null
        !instance.getTasks().isEmpty()
        def taskId = instance.getTasks().get(0).getId();

        bpmTaskService.claim(taskId, "admin");

        bpmTaskService.complete(CompleteTaskRequest.builder()
                .taskId(instance.getTasks().get(0).getId())
                .completeUserId("admin")
                .completeUserName("admin")
                .nextClaimUserId("admin")
                .build());


    }

}
