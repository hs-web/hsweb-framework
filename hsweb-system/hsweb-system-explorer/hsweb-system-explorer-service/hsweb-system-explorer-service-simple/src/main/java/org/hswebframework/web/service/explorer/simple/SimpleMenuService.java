/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.service.explorer.simple;

import org.hswebframework.web.dao.explorer.MenuDao;
import org.hswebframework.web.entity.authorization.ActionEntity;
import org.hswebframework.web.entity.explorer.MenuEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.AbstractTreeSortService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Service("menuService")
public class SimpleMenuService
        extends AbstractTreeSortService<MenuEntity<MenuEntity, ActionEntity>, String>
        implements MenuService {

    //dao api
    private MenuDao menuDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Autowired
    public void setMenuDao(MenuDao menuDao) {
        this.menuDao = menuDao;
    }

    @Override
    public MenuDao getDao() {
        return menuDao;
    }

    @Override
    public List<MenuEntity<MenuEntity, ActionEntity>> getByPermissionId(List<String> permissionId) {
        return createQuery().noPaging().where().in("permissionId", permissionId).list();
    }

    @Override
    public MenuEntity<MenuEntity, ActionEntity> getByPermissionId(String permissionId) {
        return createQuery().noPaging().where().is("permissionId", permissionId).single();
    }
}
