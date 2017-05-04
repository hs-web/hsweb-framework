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
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.entity.menu.MenuGroupEntity;
import org.hswebframework.web.entity.menu.SimpleMenuGroupEntity;
import org.hswebframework.web.starter.convert.FastJsonHttpMessageConverter;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 * TODO 完善单元测试
 *
 * @author hsweb-generator-online
 */
public class MenuGroupTests extends SimpleWebApplicationTests {

    @Autowired
    private FastJsonHttpMessageConverter fastJsonHttpMessageConverter;

    @Test
    public void testCrud() throws Exception {
        MenuGroupEntity entity = entityFactory.newInstance(MenuGroupEntity.class);
        //todo 设置测试属性
        entity.setName("test");
        entity.setId("test");

        // test add data
        String requestBody = JSON.toJSONString(entity);
        JSONObject result = testPost("/menu-group").setUp(setup -> setup.contentType(MediaType.APPLICATION_JSON).content(requestBody)).exec().resultAsJson();
        Assert.assertEquals(200, result.get("status"));
        String id = result.getString("result");
        Assert.assertNotNull(id);
        entity.setId(id);
        // test get data
        result = testGet("/menu-group/" + id).exec().resultAsJson();
        entity = result.getObject("result", entityFactory.getInstanceType(MenuGroupEntity.class));

        Assert.assertEquals(200, result.get("status"));
        Assert.assertNotNull(result.getJSONObject("result"));

        Assert.assertEquals(fastJsonHttpMessageConverter.converter(entity),
                fastJsonHttpMessageConverter.converter(result.getObject("result", entityFactory.getInstanceType(MenuGroupEntity.class))));
        //todo 修改测试属性
        MenuGroupEntity newEntity = entityFactory.newInstance(MenuGroupEntity.class);
        newEntity.setName("test");

        result = testPut("/menu-group/" + id)
                .setUp(setup ->
                        setup.contentType(MediaType.APPLICATION_JSON)
                                .content(JSON.toJSONString(newEntity)))
                .exec().resultAsJson();
        Assert.assertEquals(200, result.get("status"));

        result = testGet("/menu-group/" + id).exec().resultAsJson();
        result = result.getJSONObject("result");
        Assert.assertNotNull(result);

        result = testDelete("/menu-group/" + id).exec().resultAsJson();
        Assert.assertEquals(200, result.get("status"));

        result = testGet("/menu-group/" + id).exec().resultAsJson();
        Assert.assertEquals(404, result.get("status"));
    }
}
