package org.hswebframework.web.task;

import java.util.List;

public interface TaskExecutor {

    List<Task> findAll();

    long total();

    Task createTask(String jobId);

    Task createTask(JobDetail jobDetail);


}
