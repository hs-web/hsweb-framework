package org.hswebfarmework.web.controller.form;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebfarmework.web.entity.form.DynamicFormColumnEntity;
import org.hswebframework.web.logging.AccessLogger;
import  org.hswebfarmework.web.service.form.DynamicFormColumnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  动态表单
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.dynamicFormColumn:dynamicFormColumn}")
@Authorize(permission = "dynamicFormColumn")
@AccessLogger("动态表单")
public class DynamicFormColumnController implements SimpleGenericEntityController<DynamicFormColumnEntity, String, QueryParamEntity> {

    private DynamicFormColumnService dynamicFormColumnService;
  
    @Autowired
    public void setDynamicFormColumnService(DynamicFormColumnService dynamicFormColumnService) {
        this.dynamicFormColumnService = dynamicFormColumnService;
    }
  
    @Override
    public DynamicFormColumnService getService() {
        return dynamicFormColumnService;
    }
}
