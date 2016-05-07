package org.hsweb.web.controller.module;

import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.bean.po.module.Module;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.service.module.ModuleService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 系统模块控制器，继承自GenericController,使用rest+json
 * Created by generator 2015-8-26 11:22:11
 */
@RestController
@RequestMapping(value = "/module")
@AccessLogger("系统模块管理")
@Authorize(module = "module")
public class ModuleController extends GenericController<Module, String> {

    //默认服务类
    @Resource
    private ModuleService moduleService;

    @Override
    public ModuleService getService() {
        return this.moduleService;
    }


}
