package org.hswebframework.web.service.form;

import org.hswebframework.web.entity.form.DynamicFormColumnEntity;
import org.hswebframework.web.entity.form.DynamicFormEntity;

import java.util.List;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface FormDeployService {
    void deploy(String formId);

    void deploy(DynamicFormEntity form, List<DynamicFormColumnEntity> columns, boolean updateMeta);

    void unDeploy(String formId);
}
