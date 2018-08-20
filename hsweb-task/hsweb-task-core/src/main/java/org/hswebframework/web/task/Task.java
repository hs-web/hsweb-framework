package org.hswebframework.web.task;

import lombok.SneakyThrows;
import org.hswebframework.web.task.enums.TaskExecuteStatus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public interface Task {

    String getId();

    String getJobId();

    JobDetail getJob();

    long getLastExecuteTime();

    long getCreateTime();

    TaskExecuteStatus getStatus();

    String getCreator();

    String getSubmitor();

    long getTimeout();

    @SneakyThrows
    default TaskOperationResult execute() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<TaskOperationResult> reference = new AtomicReference<>();
        execute(result -> {
            reference.set(result);
            latch.countDown();
        });
        latch.await(getTimeout(), TimeUnit.MILLISECONDS);
        return reference.get();

    }

    void execute(Consumer<TaskOperationResult> onExecute);

    void cancel(Consumer<TaskOperationResult> onExecute);

    void suspend(Consumer<TaskOperationResult> onExecute);

    void interrupt(Consumer<TaskOperationResult> onExecute);

    void start(Consumer<TaskOperationResult> onExecute);

}
