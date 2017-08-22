package org.hswebframework.web.workflow.flowable;

import com.alibaba.fastjson.JSON;
import org.activiti.engine.*;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.hswebframework.web.workflow.flowable.service.BpmActivityService;
import org.hswebframework.web.workflow.flowable.service.BpmProcessService;
import org.hswebframework.web.workflow.flowable.service.BpmTaskService;
import org.hswebframework.web.workflow.flowable.service.imp.BpmActivityServiceImp;
import org.hswebframework.web.workflow.flowable.utils.FlowableAbstract;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author wangwei
 * @Date 2017/8/1.
 */
public class ControllerTest extends SimpleWebApplicationTests {

    @Autowired
    BpmActivityService bpmActivityService;
    @Autowired
    BpmProcessService bpmProcessService;
    @Autowired
    BpmTaskService bpmTaskService;

    public ProcessInstance start() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("username", "admin");
        map.put("leader", "zhangsan");
        map.put("team", "lisi");
        map.put("people", "wangwu");
        return bpmProcessService.startProcessInstance("1", "testid", null, null, "流程名", map);
    }

    // 流程图元数据test
    @Test
    public void activityImplTest() {
        ActivityImpl activity = bpmTaskService.selectActivityImplByTask("");
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("testid").latestVersion().active().singleResult();
        Map<String, List<String>> map = bpmActivityService.getNextClaim(processDefinition.getId(),activity.getId());
        System.out.println("=========>>>");
        System.out.println(JSON.toJSONString(map));
        System.out.println("=========>>>");
    }

    // 流程流转test
    @Test
    public void processInstanceTest() {
        ProcessInstance processInstance = null;
        try {
            processInstance = start();
            Assert.assertNotNull(processInstance);
            System.out.println("流程已启动:" + processInstance.toString());
            int i = bpmProcessService.getProcessInstances(0, 10, "test").size();
            Assert.assertEquals(i,1);
            System.out.println("当前活动流程数:" + i);
            System.out.println("当前流程节点ID_" + processInstance.getActivityId());
        } catch (Exception e) {
            System.out.println("启动流程失败" + e);
        }
        System.out.println("=========>>>");
        List<Task> tasks = bpmTaskService.selectTaskByProcessId(processInstance.getId());
        Assert.assertNotNull(tasks);
        System.out.println("当前环节:"+tasks.get(0).getName()+"__办理人:"+tasks.get(0).getAssignee());
        System.out.println("=========>>>");
        ActivityImpl activity = bpmActivityService.getActivityByProcInstId(processInstance.getProcessDefinitionId(), processInstance.getId());
        System.out.println("当前流程图元");
        System.out.println(activity);
        System.out.println("=========>>>");
        System.out.println("=========流程流转下一步，下一步为并行网关。>>>");
//        for (Task task:tasks) {
//            bpmTaskService.complete(task.getId(), "zhangsan",null,null);
//        }
        tasks = bpmTaskService.selectTaskByProcessId(processInstance.getId());
        Assert.assertNotNull(tasks);
        for (Task task:tasks) {
            System.out.println("当前环节:"+task.getName()+"__id:"+task.getId()+"__办理人:"+task.getAssignee());

            activity = bpmTaskService.selectActivityImplByTask(task.getId());
            System.out.println("当前节点图元id:"+activity.getId());
            System.out.println(activity);
            System.out.println("=========>>>");
            Map<String, List<String>> map = bpmActivityService.getNextClaim(task.getProcessDefinitionId(),activity.getId());
            System.out.println("下一步执行json:"+JSON.toJSONString(map));
        }
        System.out.println("=========>>>");
        activity = bpmActivityService.getActivityByProcInstId(processInstance.getProcessDefinitionId(), processInstance.getId());
        System.out.println("当前流程图元");
        System.out.println(activity);
        System.out.println("=========>>>");

    }

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void task(){
        ActivityImpl activity = bpmTaskService.selectActivityImplByTask("");
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("testid").orderByProcessDefinitionVersion().desc().singleResult();
        Map<String, List<String>> map = bpmActivityService.getNextClaim(processDefinition.getId(),activity.getId());
        System.out.println("=========>>>");
        System.out.println(JSON.toJSONString(map));
        System.out.println("=========>>>");
    }

    @Configuration
    public static class config {
        @Autowired(required = false)
        private List<SessionFactory> sessionFactories;

        @Bean
        public ProcessEngineConfigurationConfigurer processEngineConfigurationConfigurer() {
            return configuration -> {
                configuration
                        .setAsyncExecutorActivate(false)
                        .setJobExecutorActivate(false)
                        .setActivityFontName("宋体")
                        .setLabelFontName("宋体")
                        .setAnnotationFontName("宋体");

                if (sessionFactories != null) {
                    configuration.setCustomSessionFactories(sessionFactories);
                }
            };
        }
    }
}
