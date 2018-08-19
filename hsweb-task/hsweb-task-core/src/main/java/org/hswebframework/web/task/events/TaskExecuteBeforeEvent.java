package org.hswebframework.web.task.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.task.Task;

@AllArgsConstructor
@Getter
public class TaskExecuteBeforeEvent {
    private Task task;

    private String executionId;

}
