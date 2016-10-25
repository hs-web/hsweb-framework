/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.mybatis.user;

import org.hsweb.web.bean.common.InsertParam;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.dao.user.UserMapper;
import org.hsweb.web.mybatis.AbstractTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * @author zhouhao
 */
public class UserMapperTest extends AbstractTestCase {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testInsert() {
        User user = new User();
        user.setId("test");
        user.setUsername("admin");
        user.setName("test");
        user.setCreateDate(new Date());
        int i = userMapper.insert(InsertParam.build(user));
        Assert.assertEquals(i, 1);
    }

    @Test
    public void testQuery() {
        userMapper.select(QueryParam.build());
    }
}
