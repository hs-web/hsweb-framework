package org.hswebframework.web.service.schedule;

import org.quartz.spi.MutableTrigger;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface ScheduleTriggerBuilder {
    MutableTrigger buildTrigger(String config);
}
