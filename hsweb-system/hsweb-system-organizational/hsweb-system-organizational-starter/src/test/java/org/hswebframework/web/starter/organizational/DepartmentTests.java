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

package org.hswebframework.web.starter.organizational;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.entity.organizational.DepartmentEntity;
import org.hswebframework.web.starter.convert.FastJsonGenericHttpMessageConverter;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 * TODO 完善单元测试
 *
 * @author hsweb-generator-online
 */
public class DepartmentTests extends SimpleWebApplicationTests {

    @Autowired
    private FastJsonGenericHttpMessageConverter fastJsonHttpMessageConverter;

    @Test
    public void testCrud() throws Exception {
        DepartmentEntity entity = entityFactory.newInstance(DepartmentEntity.class);
        //todo 设置测试属性
        entity.setName("test");
        entity.setCode("2");
        // test add data
        String requestBody = JSON.toJSONString(entity);
        JSONObject result = testPost("/department").setUp(setup -> setup.contentType(MediaType.APPLICATION_JSON).content(requestBody)).exec().resultAsJson();
        Assert.assertEquals(200, result.get("status"));
        String id = result.getString("result");
        Assert.assertNotNull(id);
        entity.setId(id);
        // test get data
        result = testGet("/department/" + id).exec().resultAsJson();
        entity = result.getObject("result", entityFactory.getInstanceType(DepartmentEntity.class));

        Assert.assertEquals(200, result.get("status"));
        Assert.assertNotNull(result.getJSONObject("result"));

        Assert.assertEquals(fastJsonHttpMessageConverter.converter(entity),
                fastJsonHttpMessageConverter.converter(result.getObject("result", entityFactory.getInstanceType(DepartmentEntity.class))));
        //todo 修改测试属性
        DepartmentEntity newEntity = entityFactory.newInstance(DepartmentEntity.class);
        newEntity.setName("test");

        result = testPut("/department/" + id)
                .setUp(setup ->
                        setup.contentType(MediaType.APPLICATION_JSON)
                                .content(JSON.toJSONString(newEntity)))
                .exec().resultAsJson();
        Assert.assertEquals(200, result.get("status"));

        result = testGet("/department/" + id).exec().resultAsJson();
        result = result.getJSONObject("result");
        Assert.assertNotNull(result);

        result = testDelete("/department/" + id).exec().resultAsJson();
        Assert.assertEquals(200, result.get("status"));

        result = testGet("/department/" + id).exec().resultAsJson();
        Assert.assertEquals(404, result.get("status"));
    }
}
