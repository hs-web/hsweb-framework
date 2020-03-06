package org.hswebframework.web.authorization.basic.define;

import lombok.SneakyThrows;
import org.hswebframework.web.authorization.annotation.*;
import org.hswebframework.web.authorization.define.AopAuthorizeDefinition;
import org.hswebframework.web.authorization.define.ResourceDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class DefaultBasicAuthorizeDefinitionTest {


    @Test
    @SneakyThrows
    public void testCustomAnn() {
        AopAuthorizeDefinition definition =
                DefaultBasicAuthorizeDefinition.from(TestController.class, TestController.class.getMethod("test"));

        ResourceDefinition resource = definition.getResources()
                .getResource("test").orElseThrow(NullPointerException::new);

        Assert.assertNotNull(resource);

        Assert.assertTrue(resource.hasAction(Arrays.asList("add")));

        Assert.assertTrue(resource.getAction("add")
                .map(act->act.getDataAccess().getType("user_own_data"))
                .isPresent());
    }

    @Test
    @SneakyThrows
    public void testNoMerge() {
        AopAuthorizeDefinition definition =
                DefaultBasicAuthorizeDefinition.from(TestController.class, TestController.class.getMethod("noMerge"));
        Assert.assertTrue(definition.getResources().isEmpty());
    }


    @Resource(id = "test", name = "测试")
    public class TestController implements GenericController {

        @Authorize(merge = false)
        public void noMerge(){

        }

    }

    public interface GenericController {

        @CreateAction
        @UserOwnData
        default void test(){

        }
    }


}