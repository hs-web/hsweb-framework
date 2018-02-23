package org.hswebframework.web.controller.module;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.entity.module.ModuleEntity;
import org.hswebframework.web.service.module.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统自定义模块
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.module:module}")
@Authorize(permission = "module", description = "自定义模块")
@Api(tags = "自定义模块", value = "自定义模块")
public class ModuleController implements SimpleGenericEntityController<ModuleEntity, String, QueryParamEntity> {

    private ModuleService moduleService;

    @Autowired
    public void setModuleService(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @Override
    public ModuleService getService() {
        return moduleService;
    }
}
