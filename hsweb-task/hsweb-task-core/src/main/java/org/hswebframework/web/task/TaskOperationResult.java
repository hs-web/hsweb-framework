package org.hswebframework.web.task;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.task.enums.TaskExecuteStatus;

@Getter
@Setter
public class TaskOperationResult {
    private String taskId;

    private String jobId;

    private TaskExecuteStatus status;

    private boolean success;

    private Throwable cause;


}
