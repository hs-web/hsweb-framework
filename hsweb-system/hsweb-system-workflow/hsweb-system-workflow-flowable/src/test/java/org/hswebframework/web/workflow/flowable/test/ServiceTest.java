package org.hswebframework.web.workflow.flowable.test;

import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.hswebframework.web.workflow.flowable.service.BpmActivityService;
import org.hswebframework.web.workflow.flowable.utils.FlowableAbstract;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * Created by zhouhao on 2017/7/20.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ServiceTest extends FlowableAbstract {

    @Autowired
    BpmActivityService bpmActivityService;

    @Test
    public void getProcessDefinition(){
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("test").singleResult();
        System.out.println(processDefinition);
    }

    @Test
    public void getHiTask(){
        //查出历史信息
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId("5").taskAssignee("admin").singleResult();

        Task task = taskService.createTaskQuery().taskId("123456").singleResult();

    }
}