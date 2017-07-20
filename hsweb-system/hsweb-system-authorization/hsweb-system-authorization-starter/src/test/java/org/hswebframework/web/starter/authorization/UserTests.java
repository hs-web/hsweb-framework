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

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationInitializeService;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.service.authorization.PasswordStrengthValidator;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.service.authorization.UsernameValidator;
import org.hswebframework.web.tests.SimpleWebApplicationTests;
import org.hswebframework.web.validate.ValidationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class UserTests extends SimpleWebApplicationTests {

    @Configuration
    public static class Config {
        @Bean
        public PasswordStrengthValidator passwordStrengthValidator() {
            return new PasswordStrengthValidator() {
                @Override
                public boolean validate(String data) {
                    return data.length() >= 4;
                }

                @Override
                public String getErrorMessage() {
                    return "密码强度太弱";
                }
            };
        }

        @Bean
        public UsernameValidator usernameValidator() {
            return (username) -> username.length() >= 4;
        }
    }

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationInitializeService authenticationInitializeService;

    @After
    public void clear() throws SQLException {
        sqlExecutor.delete("delete from s_user");
    }

    public UserEntity createTestUser() {
        UserEntity userEntity = userService.createEntity();
        userEntity.setName("测试");
        userEntity.setUsername("test");
        userEntity.setPassword("password_1234");
        userEntity.setCreateTimeNow();
        userEntity.setCreatorId("admin");
        userService.insert(userEntity);
        return userEntity;
    }

    @Test
    public void testInitAuth() {
        UserEntity entity = createTestUser();
        Authentication authentication = authenticationInitializeService.initUserAuthorization(entity.getId());
        Assert.assertNotNull(authentication);
        Assert.assertEquals(authentication.getUser().getUsername(), entity.getUsername());
    }

    @Test
    public void testCRUD() {
        UserEntity userEntity = userService.createEntity();
        userEntity.setName("测试");
        userEntity.setUsername("test");
        userEntity.setPassword("123");
        userEntity.setCreatorId("admin");
        userEntity.setCreateTimeNow();
        try {
            userService.insert(userEntity);
            Assert.assertTrue(false);
        } catch (ValidationException e) {
            Assert.assertEquals(e.getResults().get(0).getMessage(), "密码强度太弱");
        }
        userEntity.setPassword("password_1234");
        String id = userService.insert(userEntity);

        UserEntity newUserEntity = userEntity.clone();
        newUserEntity.setUsername("test2");
        String anotherId = userService.insert(newUserEntity);

        Assert.assertNotNull(id);
        Assert.assertEquals(userEntity.getPassword().length(), 32);

        UserEntity entityInDb = userService.selectByUsername(userEntity.getUsername());
        Assert.assertEquals(entityInDb.getStatus(), DataStatus.STATUS_ENABLED);
        Assert.assertNotNull(entityInDb.getCreateTime());

        Assert.assertEquals(entityInDb.getPassword(), userService.encodePassword("password_1234", entityInDb.getSalt()));

        entityInDb = userService.selectByUsername(userEntity.getUsername());
        Assert.assertEquals(entityInDb.getStatus(), DataStatus.STATUS_ENABLED);
        Assert.assertNotNull(entityInDb.getCreateTime());
        try {
            userService.updatePassword(id, "test", "test");
            Assert.assertTrue(false);
        } catch (ValidationException e) {
            Assert.assertEquals(e.getResults().get(0).getMessage(), "{old_password_error}");
        }
        userService.updatePassword(id, "password_1234", "password_2345");
        entityInDb = userService.selectByUsername(userEntity.getUsername());
        Assert.assertEquals(entityInDb.getPassword(), userService.encodePassword("password_2345", entityInDb.getSalt()));

        entityInDb.setId(anotherId);
        entityInDb.setName("新名字");
        try {
            userService.update(anotherId, entityInDb);
            Assert.assertTrue(false);
        } catch (ValidationException e) {
            Assert.assertEquals(e.getResults().get(0).getMessage(), "{username_exists}");
        }
        entityInDb.setId(id);
        userService.update(id, entityInDb);
        entityInDb = userService.selectByUsername(userEntity.getUsername());
        Assert.assertEquals("新名字", entityInDb.getName());


        userService.disable(id);
        entityInDb = userService.selectByUsername(userEntity.getUsername());
        Assert.assertEquals(DataStatus.STATUS_DISABLED, entityInDb.getStatus());

        userService.enable(id);
        entityInDb = userService.selectByUsername(userEntity.getUsername());
        Assert.assertEquals(DataStatus.STATUS_ENABLED, entityInDb.getStatus());

    }
}
