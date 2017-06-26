package org.hswebfarmework.web.controller.form;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebfarmework.web.entity.form.DynamicFormEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebfarmework.web.service.form.DynamicFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 动态表单
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.dynamic/form:dynamic/form}")
@Authorize(permission = "dynamic-form")
@AccessLogger("动态表单")
public class DynamicFormController implements SimpleGenericEntityController<DynamicFormEntity, String, QueryParamEntity> {

    private DynamicFormService dynamicFormService;

    @Autowired
    public void setDynamicFormService(DynamicFormService dynamicFormService) {
        this.dynamicFormService = dynamicFormService;
    }

    @Override
    public DynamicFormService getService() {
        return dynamicFormService;
    }
}
