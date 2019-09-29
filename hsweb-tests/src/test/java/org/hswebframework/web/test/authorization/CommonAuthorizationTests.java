package org.hswebframework.web.test.authorization;


import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.entity.authorization.bind.BindRoleUserEntity;
import org.hswebframework.web.entity.authorization.bind.SimpleBindRoleUserEntity;
import org.hswebframework.web.service.authorization.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public abstract class CommonAuthorizationTests {

    @Autowired
    UserService userService;


    @Test
    public void testUserCrud() {
        BindRoleUserEntity userEntity=new SimpleBindRoleUserEntity();
        userEntity.setName("test");
        userEntity.setUsername("admin");
        userEntity.setPassword("admin");
        userEntity.setRoles(Arrays.asList("admin"));
        String id = userService.insert(userEntity);
        Assert.assertNotNull(id);

    }

}
