package org.hswebframework.web.task.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.task.Task;
import org.hswebframework.web.task.enums.TaskExecuteStatus;

@AllArgsConstructor
@Getter
public class TaskStatusChangedEvent {

    private TaskExecuteStatus before;

    private TaskExecuteStatus after;

    private Task task;
}
