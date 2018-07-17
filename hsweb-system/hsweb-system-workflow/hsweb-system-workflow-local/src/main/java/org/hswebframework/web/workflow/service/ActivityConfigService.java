package org.hswebframework.web.workflow.service;

import org.hswebframework.web.service.CrudService;
import org.hswebframework.web.workflow.dao.entity.ActivityConfigEntity;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface ActivityConfigService extends CrudService<ActivityConfigEntity, String> {

    ActivityConfigEntity selectByProcessDefineIdAndActivityId(String processDefineId, String activityId);

    ActivityConfigEntity selectByProcessDefineKeyAndActivityId(String processDefineKey, String activityId);


}
