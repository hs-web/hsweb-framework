package org.hswebframework.web.workflow.flowable.utils;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.task.Comment;

import java.util.List;

/**
 * Created by Administrator on 2016/4/20 0020.
 */
public class JumpTaskCmd implements Command<Comment> {
    protected String executionId;
    protected String activityId;


    public JumpTaskCmd(String executionId, String activityId) {
        this.executionId = executionId;
        this.activityId = activityId;
    }

    @Override
    public Comment execute(CommandContext commandContext) {

        for (TaskEntity taskEntity : commandContext.getTaskEntityManager().findTasksByExecutionId(executionId)) {
            commandContext.getTaskEntityManager().deleteTask(taskEntity, "jump", false);
        }
        ExecutionEntity executionEntity = Context.getCommandContext().getExecutionEntityManager().findExecutionById(executionId);
        ProcessDefinitionImpl processDefinition = executionEntity.getProcessDefinition();
        ActivityImpl activity = processDefinition.findActivity(activityId);
        executionEntity.executeActivity(activity);

        return null;
    }
}
