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

package org.hswebframework.web.starter.dictionary;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.entity.dictionary.DictionaryEntity;
import org.hswebframework.web.entity.dictionary.DictionaryItemEntity;
import org.hswebframework.web.entity.dictionary.SimpleDictionaryItemEntity;
import org.hswebframework.web.starter.convert.FastJsonGenericHttpMessageConverter;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

/**
 * TODO 完善单元测试
 *
 * @author hsweb-generator-online
 */
public class DictionaryTests extends SimpleWebApplicationTests {

    @Autowired
    private FastJsonGenericHttpMessageConverter fastJsonHttpMessageConverter;

    @Test
    public void testCrud() throws Exception {
        DictionaryEntity entity = entityFactory.newInstance(DictionaryEntity.class);
        //todo 设置测试属性
        entity.setName("test");
        entity.setCreatorId("admin");
        entity.setCreateTime(System.currentTimeMillis());
        entity.setId("test");
        entity.setDescribe("test");
        entity.setClassifiedId("test");
        entity.setStatus(DataStatus.STATUS_ENABLED);
        String json = "[" +
                "{'value':'1','text':'水果','children':" +
                "[" +
                "{'value':'101','text':'苹果'," +
                "'children':[" +
                "{'value':'10102','text':'红富士'}" +
                ",{'value':'10103','text':'青苹果'}" +
                //使用表达式进行解析
                ",{'value':'10105','text':'其他苹果'" +
                ",'textExpression':'${#value}[${#context[otherApple]}]'" +
                ",'valueExpression':'${(#context.put(\\'otherApple\\',#pattern.split(\"[ \\\\[ \\\\]]\")[1])==null)?#value:#value}'" +
                "}" +
                "]}" +
                ",{'value':'102','text':'梨子'}]" +
                "}" +
                ",{'value':'2','text':'蔬菜'}" +
                "]";

        List<DictionaryItemEntity> itemEntities = JSON.parseArray(json, DictionaryItemEntity.class);
        entity.setItems(itemEntities);
        // test add data
        String requestBody = JSON.toJSONString(entity);
        JSONObject result = testPost("/dictionary").setUp(setup -> setup.contentType(MediaType.APPLICATION_JSON).content(requestBody)).exec().resultAsJson();
        Assert.assertEquals(200, result.get("status"));
        String id = result.getString("result");
        Assert.assertNotNull(id);
        entity.setId(id);
        // test get data
        result = testGet("/dictionary/" + id).exec().resultAsJson();
        entity = result.getObject("result", entityFactory.getInstanceType(DictionaryEntity.class));

        Assert.assertEquals(200, result.get("status"));
        Assert.assertNotNull(result.getJSONObject("result"));

        Assert.assertEquals(fastJsonHttpMessageConverter.converter(entity),
                fastJsonHttpMessageConverter.converter(result.getObject("result", entityFactory.getInstanceType(DictionaryEntity.class))));
        //todo 修改测试属性
        DictionaryEntity newEntity = entityFactory.newInstance(DictionaryEntity.class);
        newEntity.setName("test");

        result = testPut("/dictionary/" + id)
                .setUp(setup ->
                        setup.contentType(MediaType.APPLICATION_JSON)
                                .content(JSON.toJSONString(newEntity)))
                .exec().resultAsJson();
        Assert.assertEquals(200, result.get("status"));

        result = testGet("/dictionary/" + id).exec().resultAsJson();
        result = result.getJSONObject("result");
        Assert.assertNotNull(result);

        result = testDelete("/dictionary/" + id).exec().resultAsJson();
        Assert.assertEquals(200, result.get("status"));

        result = testGet("/dictionary/" + id).exec().resultAsJson();
        Assert.assertEquals(404, result.get("status"));
    }
}
