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
 *
 */

package org.hswebframework.web.controller.menu;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Role;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.GenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.menu.MenuEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.menu.MenuGroupService;
import org.hswebframework.web.service.menu.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hswebframework.web.controller.message.ResponseMessage.ok;

/**
 * 菜单分组
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.menu:menu}")
@Authorize(permission = "menu")
@AccessLogger("菜单")
@Api(value = "menu-manager", description = "系统菜单管理")
public class MenuController implements GenericEntityController<MenuEntity, String, QueryParamEntity, MenuEntity> {

    private MenuService menuService;

    private MenuGroupService menuGroupService;

    @Autowired
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }

    @Autowired
    public void setMenuGroupService(MenuGroupService menuGroupService) {
        this.menuGroupService = menuGroupService;
    }

    @Override
    public MenuService getService() {
        return menuService;
    }

    @GetMapping("/user-own/list")
    @Authorize(merge = false)
    @ApiOperation("获取当前用户的菜单列表")
    public ResponseMessage<List<MenuEntity>> getUserMenuAsList(@ApiParam(hidden = true) Authentication authentication) {
        List<MenuEntity> menuEntities = menuGroupService
                .getMenuByGroupId(authentication.getRoles()
                        .stream()
                        .map(Role::getId)
                        .collect(Collectors.toList()));
        return ok(menuEntities);
    }

    @GetMapping("/user-own/tree")
    @Authorize(merge = false)
    @ApiOperation("获取当前用户的菜单树")
    public ResponseMessage<List<MenuEntity>> getUserMenuAsTree(@ApiParam(hidden = true) Authentication authentication) {
        List<MenuEntity> menuEntities = getUserMenuAsList(authentication).getResult();
        //list转tree
        return ok(TreeSupportEntity.list2tree(menuEntities, (menu, children) -> menu.setChildren(new ArrayList<>(children))));
    }
}
