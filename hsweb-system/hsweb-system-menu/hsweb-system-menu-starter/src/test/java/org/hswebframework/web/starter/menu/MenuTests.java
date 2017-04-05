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

package org.hswebframework.web.starter.menu;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.entity.menu.MenuEntity;
import org.hswebframework.web.service.menu.MenuService;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */

public class MenuTests extends SimpleWebApplicationTests {

    @Autowired
    MenuService menuService;

    @After
    public void clear() throws SQLException {
        sqlExecutor.delete("delete from s_menu");
    }

    public MenuEntity<MenuEntity> createMenu(String name) {
        MenuEntity<MenuEntity> menuEntity = menuService.createEntity();
        menuEntity.setName(name);
        return menuEntity;
    }

    @Test
    public void testCrud() throws Exception {
        MenuEntity<MenuEntity> menuEntity = createMenu("测试1");
        menuEntity.setSortIndex(1L);
        MenuEntity<MenuEntity> child1 = createMenu("测试2");
        MenuEntity<MenuEntity> child3 = createMenu("测试2");
        menuEntity.setChildren(Arrays.asList(child1, child3));

        String id = menuService.insert(menuEntity);

        System.out.println(JSON.toJSONString(menuService.select()));
        assertNotNull(id);

    }
}
