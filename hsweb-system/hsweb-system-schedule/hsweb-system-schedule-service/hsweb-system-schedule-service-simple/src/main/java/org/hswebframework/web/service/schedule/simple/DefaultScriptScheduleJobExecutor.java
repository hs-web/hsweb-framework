package org.hswebframework.web.service.schedule.simple;

import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.web.entity.schedule.ScheduleJobEntity;
import org.hswebframework.web.service.schedule.ScheduleJobExecutor;
import org.hswebframework.web.service.schedule.ScheduleJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author zhouhao
 */
@Service
public class DefaultScriptScheduleJobExecutor implements ScheduleJobExecutor {

    private ScheduleJobService scheduleJobService;

    @Autowired
    public void setScheduleJobService(ScheduleJobService scheduleJobService) {
        this.scheduleJobService = scheduleJobService;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Object doExecuteJob(String jobId, Map<String, Object> parameter) {
        try {
            ScheduleJobEntity jobEntity = scheduleJobService.selectByPk(jobId);
            if (null == jobEntity) {
                return null;
            }
            DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(jobEntity.getLanguage());

            String jobMd5 = DigestUtils.md5Hex(jobEntity.getScript());
            //脚本发生变化，重新编译执行
            if (engine.getContext(jobId) == null || !jobMd5.equals(engine.getContext(jobId).getMd5())) {
                engine.compile(jobId, jobEntity.getScript());
            }
            return engine.execute(jobId, parameter).getIfSuccess();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
