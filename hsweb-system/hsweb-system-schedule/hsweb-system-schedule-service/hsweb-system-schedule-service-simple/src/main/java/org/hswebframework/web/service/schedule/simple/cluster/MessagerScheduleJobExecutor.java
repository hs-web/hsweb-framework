package org.hswebframework.web.service.schedule.simple.cluster;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.entity.schedule.ScheduleJobEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.message.MessageSubscribe;
import org.hswebframework.web.message.Messager;
import org.hswebframework.web.message.builder.StaticMessageBuilder;
import org.hswebframework.web.message.builder.StaticMessageSubjectBuilder;
import org.hswebframework.web.message.support.ObjectMessage;
import org.hswebframework.web.service.schedule.ScheduleJobExecutor;
import org.hswebframework.web.service.schedule.ScheduleJobService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.ref.Reference;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hswebframework.web.message.builder.StaticMessageSubjectBuilder.*;

/**
 * @author zhouhao
 * @since 3.0
 */
@Slf4j
public class MessagerScheduleJobExecutor implements ScheduleJobExecutor {
    private ScheduleJobService scheduleJobService;

    private ScheduleJobExecutor executor;

    private Messager messager;

    public MessagerScheduleJobExecutor(ScheduleJobService scheduleJobService, ScheduleJobExecutor executor, Messager messager) {
        this.executor = executor;
        this.messager = messager;
        this.scheduleJobService = scheduleJobService;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setMessager(Messager messager) {
        this.messager = messager;
    }

    public void setExecutor(ScheduleJobExecutor executor) {
        this.executor = executor;
    }

    public void setScheduleJobService(ScheduleJobService scheduleJobService) {
        this.scheduleJobService = scheduleJobService;
    }

    private List<String> tags = new ArrayList<>();

    private List<MessageSubscribe> subscribes = new ArrayList<>();

    @PreDestroy
    public void destroy() {
        subscribes.forEach(MessageSubscribe::cancel);
    }

    @PostConstruct
    public void init() {
        for (String tag : tags) {
            MessageSubscribe subscribe = messager.subscribe(queue("quartz-job-" + tag + "-execute"))
                    .onMessage(msg -> {
                        JobExecutorMessage executorMessage = (JobExecutorMessage) msg;
                        JobExecuteResultMessage resultMessage = new JobExecuteResultMessage();
                        resultMessage.setExecuteId(executorMessage.getExecuteId());
                        try {
                            Object result = executor.doExecuteJob(executorMessage.getJobId(), executorMessage.getParameters());
                            resultMessage.setResult(result);
                        } catch (Exception e) {
                            resultMessage.setResult(e.getMessage());
                            resultMessage.setSuccess(false);
                        }
                        messager.publish(resultMessage)
                                .to(queue("quartz-job-" + executorMessage.getExecuteId() + "-result"))
                                .send();
                    });
            subscribes.add(subscribe);
        }
    }

    @Override
    public Object doExecuteJob(String jobId, Map<String, Object> parameter) {
        ScheduleJobEntity jobEntity = scheduleJobService.selectByPk(jobId);
        if (null == jobEntity) {
            return null;
        }
        if (jobEntity.getTags() == null) {
            return executor.doExecuteJob(jobId, parameter);
        }
        List<String> confTags = Arrays.asList(jobEntity.getTags().split("[,]"));
        if (confTags.isEmpty() || confTags.stream().anyMatch(tags::contains)) {
            return executor.doExecuteJob(jobId, parameter);
        }
        CountDownLatch latch = new CountDownLatch(1);
        Object[] object = new Object[1];
        Random random = new Random();

        String tag = confTags.get(random.nextInt(confTags.size()));
        String executeId = IDGenerator.MD5.generate();
        //先订阅执行结果
        MessageSubscribe subscribe = messager.subscribe(queue("quartz-job-" + executeId + "-result"))
                .<JobExecuteResultMessage>onMessage(message -> {
                    try {
                        object[0] = ((JobExecuteResultMessage) message).getResult();
                    } catch (Exception e) {
                        object[0] = e.getMessage();
                    } finally {
                        latch.countDown();
                    }
                });
        try {
            //发送任务执行
            messager.publish(new JobExecutorMessage(executeId, jobId, parameter))
                    .to(queue("quartz-job-" + tag + "-execute"))
                    .send();
            boolean success = latch.await(1, TimeUnit.HOURS);
            if (!success) {
                log.warn("await job execute fail");
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        } finally {
            //取消结果订阅
            subscribe.cancel();
        }
        return object[0];
    }
}
