/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.service.impl.quartz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.hsweb.commons.MD5;
import org.hsweb.commons.StringUtils;
import org.hsweb.expands.script.engine.DynamicScriptEngine;
import org.hsweb.expands.script.engine.DynamicScriptEngineFactory;
import org.hsweb.expands.script.engine.ExecuteResult;
import org.hsweb.expands.script.engine.ScriptContext;
import org.hsweb.web.bean.po.quartz.QuartzJob;
import org.hsweb.web.bean.po.quartz.QuartzJobHistory;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.dao.quartz.QuartzJobHistoryMapper;
import org.hsweb.web.dao.quartz.QuartzJobMapper;
import org.hsweb.web.service.GenericService;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.hsweb.web.service.quartz.QuartzJobHistoryService;
import org.hsweb.web.service.quartz.QuartzJobService;
import org.joda.time.DateTime;
import org.quartz.*;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.spi.MutableTrigger;
import org.quartz.spi.OperableTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hsweb.web.bean.po.quartz.QuartzJob.Property.enabled;
import static org.hsweb.web.bean.po.quartz.QuartzJob.Property.id;
import static org.hsweb.web.bean.po.quartz.QuartzJobHistory.Status.FAIL;
import static org.hsweb.web.bean.po.quartz.QuartzJobHistory.Status.SUCCESS;

/**
 * 定时调度任务服务类
 * Created by generator
 */
@Service("quartzJobService")
public class QuartzJobServiceImpl extends AbstractServiceImpl<QuartzJob, String> implements QuartzJobService {

    private static final String CACHE_KEY = "quartz-job";

    @Resource
    protected QuartzJobMapper quartzJobMapper;

    @Autowired
    protected Scheduler scheduler;

    @Resource
    protected QuartzJobHistoryService quartzJobHistoryService;

    @Resource
    protected QuartzJobHistoryMapper quartzJobHistoryMapper;

    @Override
    protected QuartzJobMapper getMapper() {
        return this.quartzJobMapper;
    }

    @Override
    @Cacheable(value = CACHE_KEY, key = "'id:'+#id")
    public QuartzJob selectByPk(String id) {
        return super.selectByPk(id);
    }

    @Override
    @CacheEvict(value = CACHE_KEY, key = "'id:'+#data.id")
    public String insert(QuartzJob data) {
        data.setEnabled(true);
        String id = super.insert(data);
        startJob(data);
        return id;
    }

    @Override
    @CacheEvict(value = CACHE_KEY, key = "'id:'+#data.id")
    public int update(QuartzJob data) {
        QuartzJob old = selectByPk(data.getId());
        assertNotNull(old, "任务不存在");
        int i = createUpdate(data).fromBean().excludes(enabled).where(id).exec();
        if (old.isEnabled()) {
            deleteJob(data.getId());
            startJob(data);
        }
        return i;
    }

    @Override
    public int saveOrUpdate(QuartzJob job) {
        throw new UnsupportedOperationException();
    }

    @Override
    @CacheEvict(value = CACHE_KEY, key = "'id:'+#id")
    public void enable(String id) {
        createUpdate().set(enabled, true).where(QuartzJob.Property.id, id).exec();
        startJob(getMapper().selectByPk(id));
    }

    @Override
    @CacheEvict(value = CACHE_KEY, key = "'id:'+#id")
    public void disable(String id) {
        createUpdate().set(enabled, false).where(QuartzJob.Property.id, id).exec();
        deleteJob(id);
    }

    @Override
    @CacheEvict(value = CACHE_KEY, key = "'id:'+#id")
    public int delete(String id) {
        deleteJob(id);
        GenericService.createDelete(quartzJobHistoryMapper).where(QuartzJobHistory.Property.jobId, id).exec();
        return super.delete(id);
    }

    void deleteJob(String id) {
        JobKey jobKey = createJobKey(id);
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            throw new BusinessException("更新任务失败", e, 500);
        }
    }


    @Override
    public List<Date> getExecTimes(String cron, int number) {
        try {
            CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
            cronTriggerImpl.setCronExpression(cron);
            return computeFireTimesBetween(cronTriggerImpl, null, new Date(), new DateTime().plusYears(5).toDate(), number);
        } catch (Exception e) {
            throw new BusinessException(e.getMessage(), e, 500);
        }
    }

    @Override
    @Transactional
    public Object execute(String id, Map<String, Object> var) {
        Assert.notNull(id, "定时任务ID错误");
        QuartzJob job = selectByPk(id);
        Assert.notNull(job, "任务不存在");
        String hisId = quartzJobHistoryService.createAndInsertHistory(id);
        String strRes = null;
        QuartzJobHistory.Status status = FAIL;
        try {
            if (logger.isDebugEnabled())
                logger.debug("start job [{}]", job.getName());
            DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(job.getLanguage());
            String scriptId = "quartz.job.".concat(id);
            try {
                if (!engine.compiled(scriptId)) {
                    engine.compile(scriptId, job.getScript());
                } else {
                    ScriptContext scriptContext = engine.getContext(scriptId);
                    //脚本发生了变化，自动重新编译
                    if (!MD5.defaultEncode(job.getScript()).equals(scriptContext.getMd5())) {
                        if (logger.isDebugEnabled())
                            logger.debug("script is changed,recompile....");
                        engine.compile(scriptId, job.getScript());
                    }
                }
            } catch (Exception e) {
                throw new BusinessException("编译任务脚本失败");
            }
            if (logger.isDebugEnabled())
                logger.debug("job running...");
            ExecuteResult result = engine.execute(scriptId, var);
            if (logger.isDebugEnabled())
                logger.debug("job end...{} ", result.isSuccess() ? "success" : "fail");
            if (result.isSuccess()) {
                Object res = result.getResult();
                if (res instanceof String)
                    strRes = ((String) res);
                else strRes = JSON.toJSONString(res);
                status = SUCCESS;
            } else {
                status = FAIL;
                if (result.getException() != null) {
                    strRes = StringUtils.throwable2String(result.getException());
                    logger.error("job failed", result.getException());
                    if (result.getException() instanceof RuntimeException) {
                        throw ((RuntimeException) result.getException());
                    }
                    throw new RuntimeException(result.getException());
                } else {
                    strRes = result.getMessage();
                    logger.error("job failed {}", strRes);
                    throw new RuntimeException(strRes);
                }
            }
        } finally {
            quartzJobHistoryService.endHistory(hisId, strRes, status);
        }
        return strRes;
    }

    public static List<Date> computeFireTimesBetween(OperableTrigger trigger,
                                                     org.quartz.Calendar cal, Date from, Date to, int num) {
        LinkedList<Date> lst = new LinkedList<>();
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
        return java.util.Collections.unmodifiableList(lst);
    }

    void startJob(QuartzJob job) {
        assertNotNull(job, "任务不存在");
        JobKey key = createJobKey(job.getId());
        JobDetail jobDetail = JobBuilder.newJob(SimpleJob.class)
                .withIdentity(key)
                .setJobData(createJobDataMap(job.getParameters()))
                .usingJobData(SimpleJobFactory.QUARTZ_ID_KEY, job.getId())
                .withDescription(job.getName() + (job.getRemark() == null ? "" : job.getRemark()))
                .build();
        MutableTrigger trigger = CronScheduleBuilder.cronSchedule(job.getCron()).build();
        trigger.setKey(createTriggerKey(job.getId()));
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new BusinessException("创建定时任务失败!", e, 500);
        }
    }

    JobDataMap createJobDataMap(String parameters) {
        JobDataMap map = new JobDataMap();
        if (!StringUtils.isNullOrEmpty(parameters)) {
            JSONArray jsonArray = JSON.parseArray(parameters);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                map.put(o.getString("key"), o.get("value"));
            }
        }
        return map;
    }

    JobKey createJobKey(String jobId) {
        return new JobKey(jobId, "hsweb.scheduler");
    }

    TriggerKey createTriggerKey(String jobId) {
        return new TriggerKey(jobId, "hsweb.scheduler");
    }


}
