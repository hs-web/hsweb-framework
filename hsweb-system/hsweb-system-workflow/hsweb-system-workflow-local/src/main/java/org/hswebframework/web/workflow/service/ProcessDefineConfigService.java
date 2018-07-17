package org.hswebframework.web.workflow.service;

import org.hswebframework.web.service.CrudService;
import org.hswebframework.web.workflow.dao.entity.ActivityConfigEntity;
import org.hswebframework.web.workflow.dao.entity.ProcessDefineConfigEntity;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface ProcessDefineConfigService extends CrudService<ProcessDefineConfigEntity, String> {

    ProcessDefineConfigEntity selectByProcessDefineId(String processDefineId);

    ProcessDefineConfigEntity selectByLatestProcessDefineKey(String processDefineKey);


}
