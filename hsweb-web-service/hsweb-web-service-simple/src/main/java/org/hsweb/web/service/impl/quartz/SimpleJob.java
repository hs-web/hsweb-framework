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

import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.utils.WebUtil;
import org.hsweb.web.service.quartz.QuartzJobHistoryService;
import org.hsweb.web.service.quartz.QuartzJobService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@DisallowConcurrentExecution
public class SimpleJob implements Job {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected QuartzJobService        quartzJobService;
    protected QuartzJobHistoryService quartzJobHistoryService;
    protected Map<String, Object>     defaultVar;
    protected User                    defaultUser;

    /**
     * 子类必须实现此构造方法，否则无法创建任务
     *
     * @param quartzJobService 定时任务服务类
     * @param historyService   定时任务历史记录服务类
     */
    public SimpleJob(QuartzJobService quartzJobService, QuartzJobHistoryService historyService) {
        this.quartzJobService = quartzJobService;
        this.quartzJobHistoryService = historyService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            WebUtil.setCurrentUser(defaultUser);
            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            String id = jobDataMap.getString(SimpleJobFactory.QUARTZ_ID_KEY);
            Map<String, Object> var = getVar();
            var.put("context", context);
            var.put("user", defaultUser);
            try {
                quartzJobService.execute(id, var);
            } catch (Throwable e) {
                throw new JobExecutionException(e);
            }
        } finally {
            WebUtil.removeCurrentUser();
        }
    }

    public void setDefaultVar(Map<String, Object> defaultVar) {
        this.defaultVar = defaultVar;
    }

    public Map<String, Object> getVar() {
        if (defaultVar == null) return new HashMap<>();
        return new HashMap<>(defaultVar);
    }

    public void setDefaultUser(User defaultUser) {
        this.defaultUser = defaultUser;
    }
}
