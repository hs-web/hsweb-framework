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
import org.hsweb.commons.MD5;
import org.hsweb.commons.StringUtils;
import org.hsweb.expands.script.engine.DynamicScriptEngine;
import org.hsweb.expands.script.engine.DynamicScriptEngineFactory;
import org.hsweb.expands.script.engine.ExecuteResult;
import org.hsweb.expands.script.engine.ScriptContext;
import org.hsweb.web.bean.po.quartz.QuartzJob;
import org.hsweb.web.bean.po.quartz.QuartzJobHistory;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.utils.WebUtil;
import org.hsweb.web.service.quartz.QuartzJobHistoryService;
import org.hsweb.web.service.quartz.QuartzJobService;
import org.hsweb.web.service.user.UserService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

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
        //初始化用户信息
        try {
            //解决定时任务获取当前用户
            WebUtil.setCurrentUser(defaultUser);
            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            String id = jobDataMap.getString(SimpleJobFactory.QUARTZ_ID_KEY);
            Assert.notNull(id, "定时任务ID错误");
            QuartzJob job = quartzJobService.selectByPk(id);
            Assert.notNull(job, "任务不存在");
            if (logger.isDebugEnabled())
                logger.debug("start job [{}],data : {}", job.getName(), jobDataMap);
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
                throw new JobExecutionException("编译任务脚本失败", e);
            }
            if (logger.isDebugEnabled())
                logger.debug("job running...");
            String hisId = quartzJobHistoryService.createAndInsertHistory(id);
            Map<String, Object> var = getVar();
            var.put("context", context);
            ExecuteResult result = engine.execute(scriptId, var);
            String strRes;
            if (logger.isDebugEnabled())
                logger.debug("job end...{} ", result.isSuccess() ? "success" : "fail");
            if (result.isSuccess()) {
                Object res = result.getResult();
                if (res instanceof String)
                    strRes = ((String) res);
                else strRes = JSON.toJSONString(res);
                quartzJobHistoryService.endHistory(hisId, strRes, QuartzJobHistory.Status.SUCCESS);
            } else {
                if (result.getException() != null) {
                    strRes = StringUtils.throwable2String(result.getException());
                    logger.error("job failed", result.getException());
                } else {
                    strRes = result.getMessage();
                    logger.error("job failed {}", strRes);
                }
                quartzJobHistoryService.endHistory(hisId, strRes, QuartzJobHistory.Status.FAIL);
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
