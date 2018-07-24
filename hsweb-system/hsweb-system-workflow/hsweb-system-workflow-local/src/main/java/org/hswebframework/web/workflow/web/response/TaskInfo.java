package org.hswebframework.web.workflow.web.response;

import lombok.Data;
import org.hswebframework.web.commons.bean.Bean;

import java.util.Date;
import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Data
public class TaskInfo implements Bean {

    private static final long serialVersionUID = 8648745689340116922L;
    private String id;
    private int    revision;
    private String owner;
    private String assignee;
    private String initialAssignee;
    private String parentTaskId;
    private String name;
    private String localizedName;
    private String description;
    private String localizedDescription;
    private int    priority;
    private Date   createTime;
    private Date   dueDate;
    private int    suspensionState;
    private String category;
    private String executionId;
    private String processInstanceId;
    private String processDefinitionId;
    private String taskDefinitionKey;
    private String formKey;
    private String eventName;

    public static TaskInfo of(org.activiti.engine.task.TaskInfo task) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.copyFrom(task);
        return taskInfo;
    }
}
