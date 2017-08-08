package org.hswebframework.web.workflow.flowable.test;

import org.activiti.engine.repository.ProcessDefinition;
import org.hswebframework.web.workflow.flowable.utils.FlowableAbstract;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by zhouhao on 2017/7/20.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ServiceTest extends FlowableAbstract {

    @Test
    public void getProcessDefinition(){
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("test").singleResult();
        System.out.println(processDefinition);
    }

}