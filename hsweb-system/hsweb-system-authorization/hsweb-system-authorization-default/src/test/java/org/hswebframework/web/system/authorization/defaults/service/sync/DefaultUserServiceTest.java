package org.hswebframework.web.system.authorization.defaults.service.sync;

import org.hswebframework.web.system.authorization.api.entity.UserEntity;
import org.hswebframework.web.system.authorization.api.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class DefaultUserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testCrud() {
        UserEntity userEntity = new UserEntity();
        userEntity.setName("test");
        userEntity.setUsername("admin");
        userEntity.setPassword("admin");

        Assert.assertTrue(userService.saveUser(userEntity));

        Assert.assertNotNull(userEntity.getId());

        userEntity.setUsername("admin2");
        userEntity.setPassword("admin2");
        userService.saveUser(userEntity);

        userService.changeState(userEntity.getId(), (byte) 1);

        userService.changePassword(userEntity.getId(),"admin2","admin");

        UserEntity entity = userService.findByUsernameAndPassword("admin", "admin").orElseThrow(NullPointerException::new);

        Assert.assertEquals(entity.getName(), userEntity.getName());

        Assert.assertEquals(entity.getStatus().byteValue(), (byte) 1);

    }

}