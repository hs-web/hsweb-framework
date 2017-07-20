package org.hswebframework.web.service.form;

import org.hswebframework.web.entity.form.DynamicFormDeployLogEntity;
import org.hswebframework.web.service.CrudService;

/**
 * 表单发布日志 服务类
 *
 * @author hsweb-generator-online
 */
public interface DynamicFormDeployLogService extends CrudService<DynamicFormDeployLogEntity, String> {
    DynamicFormDeployLogEntity selectLastDeployed(String formId);

    DynamicFormDeployLogEntity selectDeployed(String formId, long version);

    void cancelDeployed(String formId);
    
    void cancelDeployed(String formId, long version);

}
