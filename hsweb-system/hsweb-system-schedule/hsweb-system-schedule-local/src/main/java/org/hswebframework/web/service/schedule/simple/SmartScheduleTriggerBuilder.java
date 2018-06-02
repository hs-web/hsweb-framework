package org.hswebframework.web.service.schedule.simple;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.service.schedule.ScheduleTriggerBuilder;
import org.quartz.CronScheduleBuilder;
import org.quartz.spi.MutableTrigger;
import org.springframework.stereotype.Service;

/**
 * @author zhouhao
 */
@Service
public class SmartScheduleTriggerBuilder implements ScheduleTriggerBuilder {
    @Override
    public MutableTrigger buildTrigger(String config) {
        JSONObject configObj = JSON.parseObject(config);
        switch (configObj.getString("type")) {
            case "cron":
                String cron = configObj.getString("config");
                return CronScheduleBuilder.cronSchedule(cron)
                        .build();
            default:
                throw new UnsupportedOperationException(config);
        }

    }
}
