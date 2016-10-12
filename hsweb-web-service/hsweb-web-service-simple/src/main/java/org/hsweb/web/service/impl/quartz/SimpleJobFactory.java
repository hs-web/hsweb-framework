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

import org.hsweb.commons.ClassUtils;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.hsweb.web.service.config.ConfigService;
import org.hsweb.web.service.quartz.QuartzJobHistoryService;
import org.hsweb.web.service.quartz.QuartzJobService;
import org.hsweb.web.service.user.UserService;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class SimpleJobFactory implements JobFactory {

    public static final String QUARTZ_ID_KEY = "quartz.id";
    protected           Logger logger        = LoggerFactory.getLogger(this.getClass());
    @Resource
    private QuartzJobService quartzJobService;

    @Resource
    private QuartzJobHistoryService quartzJobHistoryService;

    private JobFactory defaultFactory;

    @Resource
    private UserService userService;

    @Resource
    private ConfigService configService;

    @Autowired(required = false)
    private Map<String, ExpressionScopeBean> expressionScopeBeanMap;

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        Map<String, Object> data = bundle.getJobDetail().getJobDataMap();
        Class<? extends Job> jobClass = bundle.getJobDetail().getJobClass();
        if (ClassUtils.instanceOf(jobClass, SimpleJob.class)) {
            String id = (String) data.get(QUARTZ_ID_KEY);
            Assert.notNull(id);
            try {
                SimpleJob job = (SimpleJob) jobClass.getConstructor(QuartzJobService.class, QuartzJobHistoryService.class).newInstance(quartzJobService, quartzJobHistoryService);
                String username = configService.get("quartz", "executeUserName", "admin");
                User user = userService.selectByUserName(username);
                if (user != null) {
                    user.initRoleInfo();
                    job.setDefaultUser(user);
                } else {
                    //未找到用户名
                    logger.warn("job executor user:{} not found!", username);
                }
                if (expressionScopeBeanMap != null)
                    job.setDefaultVar(new HashMap<>(expressionScopeBeanMap));
                return job;
            } catch (Exception e) {
                throw new SchedulerException("create simple job instance error", e);
            }
        } else {
            if (defaultFactory != null) return defaultFactory.newJob(bundle, scheduler);
        }
        throw new SchedulerException("job class not a SimpleJob and defaultFactory is not set!");
    }

    public void setDefaultFactory(JobFactory defaultFactory) {
        this.defaultFactory = defaultFactory;
    }
}
