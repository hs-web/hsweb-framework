/*
 * Copyright 2019 http://www.hswebframework.org
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

package org.hswebframework.web.authorization.starter;

import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.authorization.basic.configuration.EnableAopAuthorize;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.sql.SQLException;

/**
 * @author zhouhao
 */
@Configuration
@EnableAopAuthorize
public class LoginTests extends SimpleWebApplicationTests {

    @Autowired
    private UserService userService;

    @After
    public void clear() throws SQLException {
        sqlExecutor.delete("delete from s_user");
    }

    @Test
    public void testLogin() throws Exception {
        UserEntity userEntity = userService.createEntity();
        userEntity.setName("测试");
        userEntity.setUsername("test");
        userEntity.setPassword("password_1234");
        userEntity.setCreatorId("admin");
        userEntity.setCreateTimeNow();
        userService.insert(userEntity);

        JSONObject json = testPost("/authorize/login").setUp((builder) -> {
            builder.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
            builder.param("username", userEntity.getUsername());
            builder.param("password", "password_1234");
        }).exec().resultAsJson();

        org.junit.Assert.assertEquals(userEntity.getId(), json.getJSONObject("result").getString("userId"));

    }
}
