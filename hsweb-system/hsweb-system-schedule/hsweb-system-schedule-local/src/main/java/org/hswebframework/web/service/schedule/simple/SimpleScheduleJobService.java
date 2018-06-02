package org.hswebframework.web.service.schedule.simple;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.dao.schedule.ScheduleJobDao;
import org.hswebframework.web.entity.schedule.ScheduleJobEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.schedule.ScheduleJobService;
import org.hswebframework.web.service.schedule.ScheduleTriggerBuilder;
import org.quartz.*;
import org.quartz.spi.MutableTrigger;
import org.quartz.spi.OperableTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("scheduleJobService")
//@CacheConfig(cacheNames = "schedule-job")
public class SimpleScheduleJobService extends GenericEntityService<ScheduleJobEntity, String>
        implements ScheduleJobService {
    @Autowired
    private ScheduleJobDao scheduleJobDao;

    @Autowired
    protected Scheduler scheduler;

    @Autowired
    private ScheduleTriggerBuilder scheduleTriggerBuilder;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public ScheduleJobDao getDao() {
        return scheduleJobDao;
    }

    public static List<Date> computeFireTimesBetween(OperableTrigger trigger,
                                                     org.quartz.Calendar cal, Date from, Date to, int num) {
        List<Date> lst = new LinkedList<>();
        OperableTrigger t = (OperableTrigger) trigger.clone();
        if (t.getNextFireTime() == null) {
            t.setStartTime(from);
            t.setEndTime(to);
            t.computeFirstFireTime(cal);
        }
        for (int i = 0; i < num; i++) {
            Date d = t.getNextFireTime();
            if (d != null) {
                if (d.before(from)) {
                    t.triggered(cal);
                    continue;
                }
                if (d.after(to)) {
                    break;
                }
                lst.add(d);
                t.triggered(cal);
            } else {
                break;
            }
        }
        return lst;
    }

    protected void startJob(ScheduleJobEntity jobEntity) {
        try {
            if (scheduler.checkExists(createJobKey(jobEntity))) {
                return;
            }
            JobDetail jobDetail = JobBuilder
                    .newJob(DynamicJob.class)
                    .withIdentity(createJobKey(jobEntity))
                    .setJobData(createJobDataMap(jobEntity.getParameters()))
                    .usingJobData(DynamicJobFactory.JOB_ID_KEY, jobEntity.getId())
                    .withDescription(jobEntity.getName() + (jobEntity.getRemark() == null ? "" : jobEntity.getRemark()))
                    .build();
            MutableTrigger trigger = scheduleTriggerBuilder.buildTrigger(jobEntity.getQuartzConfig());
            trigger.setKey(createTriggerKey(jobEntity));

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new BusinessException("启动定时调度失败", e);
        }
    }

    protected JobDataMap createJobDataMap(String parameters) {
        JobDataMap map = new JobDataMap();
        if (!StringUtils.isEmpty(parameters)) {
            JSONArray jsonArray = JSON.parseArray(parameters);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                map.put(o.getString("key"), o.get("value"));
            }
        }
        return map;
    }

    protected JobKey createJobKey(ScheduleJobEntity jobEntity) {
        String group = jobEntity.getType() == null ? "hsweb.scheduler" : jobEntity.getType();

        return new JobKey(jobEntity.getId(), group);
    }

    protected TriggerKey createTriggerKey(ScheduleJobEntity jobEntity) {
        String group = jobEntity.getType() == null ? "hsweb.scheduler" : jobEntity.getType();

        return new TriggerKey(jobEntity.getId(), group);
    }

    @Override
    public void enable(String id) {
        Objects.requireNonNull(id);
        int size = createUpdate().set(ScheduleJobEntity.status, DataStatus.STATUS_ENABLED)
                .where(ScheduleJobEntity.id, id).exec();
        if (size > 0) {
            startJob(selectByPk(id));
        }
    }

    private void deleteJob(ScheduleJobEntity jobEntity) {
        JobKey jobKey = createJobKey(jobEntity);
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            throw new BusinessException("更新任务失败", e, 500);
        }
    }

    @Override
    public void disable(String id) {
        Objects.requireNonNull(id);
        int size = createUpdate().set(ScheduleJobEntity.status, DataStatus.STATUS_DISABLED)
                .where(ScheduleJobEntity.id, id).exec();
        if (size > 0) {
            deleteJob(selectByPk(id));
        }
    }
}
