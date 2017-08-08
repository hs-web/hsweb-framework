package org.hswebframework.web.workflow.flowable;

import com.alibaba.fastjson.JSON;
import org.activiti.engine.*;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.hswebframework.web.workflow.flowable.service.BpmActivityService;
import org.hswebframework.web.workflow.flowable.service.BpmProcessService;
import org.hswebframework.web.workflow.flowable.service.imp.BpmActivityServiceImp;
import org.hswebframework.web.workflow.flowable.utils.FlowableAbstract;
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

    public ProcessInstance start() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("username", "admin");
        return bpmProcessService.startProcessInstance("1", "test", null, null, "流程名", map);
    }

    @Test
    public void test() {
        ProcessInstance processInstance = null;
        try {
            processInstance = start();
            System.out.println("流程已启动:" + processInstance.toString());
            int i = bpmProcessService.getProcessInstances(0, 10, "test").size();
            System.out.println("当前活动流程数:" + i);
            System.out.println("当前流程节点ID_" + processInstance.getActivityId());
        } catch (Exception e) {
            System.out.println("启动流程失败" + e);
        }
        ProcessDefinitionEntity processDefinitionEntity = ((BpmActivityServiceImp)bpmProcessService).getProcessDefinition(processInstance.getProcessDefinitionId());
        ActivityImpl activity = bpmActivityService.getActivityByProcInstId(processInstance.getProcessDefinitionId(), processInstance.getId());
        System.out.println("流程节点读取");
        System.out.println(processDefinitionEntity.getActivities());
        System.out.println(activity);
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
