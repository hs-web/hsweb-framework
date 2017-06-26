/*
 *  Copyright 2016 http://www.hswebframework.org
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package org.hswebframework.web.controller.authorization;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.GenericEntityController;
import org.hswebframework.web.entity.authorization.MenuGroupEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.authorization.MenuGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜单分组
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.menu-group:menu-group}")
@Authorize(permission = "menu-group")
@AccessLogger("菜单分组")
public class MenuGroupController implements GenericEntityController<MenuGroupEntity, String, QueryParamEntity, MenuGroupEntity> {

    private MenuGroupService menuGroupService;

    @Override
    public MenuGroupEntity modelToEntity(MenuGroupEntity model, MenuGroupEntity entity) {
        return model;
    }

    @Autowired
    public void setMenuGroupService(MenuGroupService menuGroupService) {
        this.menuGroupService = menuGroupService;
    }

    @Override
    public MenuGroupService getService() {
        return menuGroupService;
    }
}
