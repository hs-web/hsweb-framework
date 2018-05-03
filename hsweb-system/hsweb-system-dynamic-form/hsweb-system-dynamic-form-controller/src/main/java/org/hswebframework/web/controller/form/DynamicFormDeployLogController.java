package org.hswebframework.web.controller.form;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.form.DynamicFormDeployLogEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.QueryByEntityService;
import  org.hswebframework.web.service.form.DynamicFormDeployLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *  表单发布日志
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.dynamic-form-log:dynamic/form-deploy-log}")
@Authorize(permission = "form-deploy-log",description = "表单发布日志")
@Api(value = "表单发布日志",tags = "动态表单-发布日志")
public class DynamicFormDeployLogController implements QueryController<DynamicFormDeployLogEntity, String, QueryParamEntity> {

    private DynamicFormDeployLogService dynamicFormDeployLogService;
  
    @Autowired
    public void setDynamicFormDeployLogService(DynamicFormDeployLogService dynamicFormDeployLogService) {
        this.dynamicFormDeployLogService = dynamicFormDeployLogService;
    }
  
    @Override
    @SuppressWarnings("unchecked")
    public DynamicFormDeployLogService getService() {
        return dynamicFormDeployLogService;
    }


}
