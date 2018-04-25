package org.hswebframework.web.controller.template;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.entity.template.TemplateEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.template.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模板
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.template:template}")
@Authorize(permission = "template")
@Api(tags = "模版管理",value = "模版管理")
public class TemplateController implements SimpleGenericEntityController<TemplateEntity, String, QueryParamEntity> {

    private TemplateService templateService;

    @Autowired
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public TemplateService getService() {
        return templateService;
    }
}
