/*
 * Copyright 2015-2016 https://github.com/hs-web
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * 系统模块(菜单)控制器,继承自{@link GenericController<Module, String>}
 *
 * @author zhouhao
 * @since 1.0
 */
@RestController
@RequestMapping(value = "/module")
@AccessLogger("系统模块管理")
@Authorize(module = "module")
public class ModuleController extends GenericController<Module, String> {

    @Resource
    private ModuleService moduleService;

    @Override
    public ModuleService getService() {
        return this.moduleService;
    }


}
