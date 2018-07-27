package org.hswebframework.web.workflow.listener;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.workflow.service.ProcessDefineConfigService;
import org.hswebframework.web.workflow.service.config.ActivityConfiguration;
import org.hswebframework.web.workflow.service.config.ProcessConfiguration;
import org.hswebframework.web.workflow.service.config.ProcessConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Component
public class GlobalEventListenerDispatcher implements ActivitiEventListener {

    @Autowired
    private ProcessConfigurationService processConfigurationService;

    @Autowired
    @Lazy
    private RuntimeService runtimeService;

    @Override
    public void onEvent(ActivitiEvent event) {
        String eventType = event.getType().name().toLowerCase();
        if (event instanceof ActivitiEntityEvent) {
            ActivitiEntityEvent entityEvent = ((ActivitiEntityEvent) event);

            Object entity = entityEvent.getEntity();
            if (entity instanceof TaskEntity) {
                //task事件
                TaskEntity task = ((TaskEntity) entity);
                ActivityConfiguration activityConfiguration = processConfigurationService.getActivityConfiguration(
                        Authentication.current().map(Authentication::getUser)
                                .map(User::getId).orElse(null)
                        , event.getProcessDefinitionId()
                        , task.getTaskDefinitionKey()
                );
                TaskEventListener listener = activityConfiguration.getTaskListener(eventType);
                if (null != listener) {
                    listener.doEvent(new TaskEvent() {
                        @Override
                        public Task getTask() {
                            return task;
                        }

                        @Override
                        public ProcessInstance getProcessInstance() {
                            return runtimeService
                                    .createProcessInstanceQuery()
                                    .processInstanceId(event.getProcessInstanceId())
                                    .singleResult();
                        }
                    });
                }
            }
        }
        if (eventType.startsWith("process")) {
            ProcessConfiguration configuration = processConfigurationService.getProcessConfiguration(event.getProcessDefinitionId());
            ProcessEventListener listener = configuration.getProcessListener(eventType);
            if (null != listener) {
                listener.doEvent(() -> runtimeService
                        .createProcessInstanceQuery()
                        .processInstanceId(event.getProcessInstanceId())
                        .singleResult());
            }
        }


    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
