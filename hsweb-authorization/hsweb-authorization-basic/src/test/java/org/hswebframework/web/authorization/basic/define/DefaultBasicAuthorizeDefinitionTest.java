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

        ResourceDefinition resource = definition
            .getResources()
            .getResource("test").orElseThrow(NullPointerException::new);

        Assert.assertNotNull(resource);

        Assert.assertTrue(resource.hasAction(Arrays.asList("add")));
        System.out.println(definition.getDimensions());
        Assert.assertFalse(definition.getDimensions().isEmpty());
        Assert.assertEquals(1, definition.getDimensions().getDimensions().size());


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
        public void noMerge() {

        }


    }

    public interface GenericController {

        @CreateAction
        @RequiresRoles("test")
        @RequiresRoles("test2")
        default void test() {

        }
    }


}