/*
 *  Copyright 2019 http://www.hswebframework.org
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

package org.hswebframework.web.starter.oauth2.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.entity.oauth2.client.OAuth2ServerConfigEntity;
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
public class OAuth2ServerConfigTests extends SimpleWebApplicationTests {

    @Autowired
    private FastJsonGenericHttpMessageConverter fastJsonHttpMessageConverter;


    @Autowired
    private OAuth2RequestService oAuth2RequestService;

//    @Test
//    public void testOAuth2() throws Exception {
//        OAuth2ServerConfigEntity entity = entityFactory.newInstance(OAuth2ServerConfigEntity.class);
//        //https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=123&redirect_uri=www.baidu.com
//        entity.setId("my_qq_test");
//        entity.setName("QQ OAuth2");
//        entity.setApiBaseUrl("https://graph.qq.com/oauth2.0/");
//        entity.setAuthUrl("authorize");
//        entity.setAccessTokenUrl("token");
//        entity.setClientId("911ab25b8a87684beba8f394f47d3de9");
//        entity.setClientSecret("2cce659031d5e1495e102be0de9e9cb0");
//        entity.setRedirectUri("http://demo.hsweb.me");
//        entity.setProvider("QQ");
//        entity.setEnabled(true);
//        //add
//        String requestBody = JSON.toJSONString(entity);
//        JSONObject result = testPost("/oauth2-server-config")
//                .setUp(setup -> setup.contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody)).exec().resultAsJson();
//        Assert.assertEquals(200, result.get("status"));
//
//        try {
//            Map meInfo = oAuth2RequestService.create("my_qq_test")
//                    .byAuthorizationCode("D8C3B5E8B55E4AAAC8EA1FB8DC0AFCEC")
//                    .request("me").get().as(Map.class);
//            System.out.println(meInfo);
//        } catch (OAuth2RequestException e) {
//            System.out.println(e.getErrorType() + ":" + e.getResponse().as(Map.class));
//        }
//    }

    @Test
    public void testCrud() throws Exception {
        OAuth2ServerConfigEntity entity = entityFactory.newInstance(OAuth2ServerConfigEntity.class);
        //todo 设置测试属性
        entity.setName("test");

        // test add data
        String requestBody = JSON.toJSONString(entity);
        JSONObject result = testPost("/oauth2-server-config").setUp(setup -> setup.contentType(MediaType.APPLICATION_JSON).content(requestBody)).exec().resultAsJson();
        Assert.assertEquals(200, result.get("status"));
        String id = result.getString("result");
        Assert.assertNotNull(id);
        entity.setId(id);
        // test get data
        result = testGet("/oauth2-server-config/" + id).exec().resultAsJson();
        entity = result.getObject("result", entityFactory.getInstanceType(OAuth2ServerConfigEntity.class));

        Assert.assertEquals(200, result.get("status"));
        Assert.assertNotNull(result.getJSONObject("result"));

        Assert.assertEquals(fastJsonHttpMessageConverter.converter(entity),
                fastJsonHttpMessageConverter.converter(result.getObject("result", entityFactory.getInstanceType(OAuth2ServerConfigEntity.class))));
        //todo 修改测试属性
        OAuth2ServerConfigEntity newEntity = entityFactory.newInstance(OAuth2ServerConfigEntity.class);
        newEntity.setName("test2");

        result = testPut("/oauth2-server-config/" + id)
                .setUp(setup ->
                        setup.contentType(MediaType.APPLICATION_JSON)
                                .content(JSON.toJSONString(newEntity)))
                .exec().resultAsJson();
        Assert.assertEquals(200, result.get("status"));

        result = testGet("/oauth2-server-config/" + id).exec().resultAsJson();
        result = result.getJSONObject("result");
        Assert.assertNotNull(result);

        result = testDelete("/oauth2-server-config/" + id).exec().resultAsJson();
        Assert.assertEquals(200, result.get("status"));

        result = testGet("/oauth2-server-config/" + id).exec().resultAsJson();
        Assert.assertEquals(404, result.get("status"));
    }
}
