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

package org.hswebframework.web.starter.authorization;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.hswebframework.expands.security.Encrypt;
import org.hswebframework.expands.security.rsa.RSAPublicEncrypt;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.sql.SQLException;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
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

        //获取publicKey
        String publicKey = testGet("/authorize/public-key").exec().resultAsJson().getString("data");
        Assert.notNull(publicKey);
        RSAPublicEncrypt publicEncrypt = Encrypt.rsa().publicEncrypt(publicKey);
        String username = Base64.encodeBase64String(publicEncrypt.encrypt("test".getBytes()));
        String password = Base64.encodeBase64String(publicEncrypt.encrypt("password_1234".getBytes()));
        JSONObject json = testPost("/authorize/login").setUp((builder) -> {
            builder.param("username", username);
            builder.param("password", password);
        }).exec().resultAsJson();

        System.out.println(json);
    }
}
