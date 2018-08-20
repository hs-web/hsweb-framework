package org.hswebframework.web.task;

public interface JobRepository {


    JobDetail findById(String id);

    JobDetail save(JobDetail detail);

    JobDetail update(JobDetail detail);

    JobDetail delete(JobDetail detail);

    void enable(String id);

    void disable(String id);

}
