package org.hswebframework.web.service.form;

import org.hswebframework.web.entity.form.DynamicFormEntity;
import org.hswebframework.web.service.CrudService;

/**
 * 动态表单 服务类
 *
 * @author hsweb-generator-online
 */
public interface DynamicFormService extends CrudService<DynamicFormEntity, String> {

    void deployAll();

    void deploy(String formId);

    void unDeploy(String formId);

}
