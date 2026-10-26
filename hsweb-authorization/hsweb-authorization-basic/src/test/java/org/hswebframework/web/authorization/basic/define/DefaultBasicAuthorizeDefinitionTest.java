package org.hswebframework.web.authorization.basic.define;

import lombok.SneakyThrows;
import org.hswebframework.web.authorization.annotation.*;
import org.hswebframework.web.authorization.define.AopAuthorizeDefinition;
import org.hswebframework.web.authorization.define.ResourceDefinition;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.hswebframework.web.authorization.simple.SimplePermission;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DefaultBasicAuthorizeDefinitionTest {

    @Test
    public void resourceLogicalControlsAuthorization() throws Exception {
        assertPermissions("and", false, false, false, true);
        assertPermissions("or", false, true, true, true);
        assertPermissions("inherit", false, false, false, true);
        assertPermissions("reset", false, true, true, true);
        assertPermissions("composed", false, false, false, true);
        AopAuthorizeDefinition reset = definition("reset");
        Assert.assertEquals(Logical.DEFAULT, reset.getResources().getLogical());
        Assert.assertFalse(reset.getResources().getResource("class-resource").isPresent());
    }

    @Test
    public void defaultWithoutClassLogicalKeepsExistingAnyResourceBehavior() throws Exception {
        AopAuthorizeDefinition definition = DefaultBasicAuthorizeDefinition.from(
            DefaultController.class, DefaultController.class.getMethod("query"));
        Assert.assertEquals(Logical.DEFAULT, definition.getResources().getLogical());
        Assert.assertTrue(definition.getResources().hasPermission(authentication("first")));
        Assert.assertTrue(definition.getResources().hasPermission(authentication("second")));
        Assert.assertFalse(definition.getResources().hasPermission(authentication()));
    }

    private void assertPermissions(String method, boolean neither, boolean first, boolean second, boolean both)
        throws Exception {
        AopAuthorizeDefinition definition = definition(method);
        Assert.assertEquals(method, neither, definition.getResources().hasPermission(authentication()));
        Assert.assertEquals(method, first, definition.getResources().hasPermission(authentication("first")));
        Assert.assertEquals(method, second, definition.getResources().hasPermission(authentication("second")));
        Assert.assertEquals(method, both, definition.getResources().hasPermission(authentication("first", "second")));
    }

    private AopAuthorizeDefinition definition(String method) throws Exception {
        return DefaultBasicAuthorizeDefinition.from(LogicalController.class, LogicalController.class.getMethod(method));
    }

    private SimpleAuthentication authentication(String... resources) {
        SimpleAuthentication auth = new SimpleAuthentication();
        auth.setPermissions(Arrays.stream(resources)
            .map(id -> SimplePermission.builder().id(id).actions(Collections.singleton("save")).build())
            .collect(Collectors.toList()));
        return auth;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Authorize(logical = Logical.AND, resources = {
        @Resource(id = "first", name = "First", actions = @ResourceAction(id = "save", name = "Save")),
        @Resource(id = "second", name = "Second", actions = @ResourceAction(id = "save", name = "Save"))
    })
    public @interface BothResources { }

    @Authorize(logical = Logical.AND)
    public static class LogicalController {
        @Authorize(logical = Logical.AND, resources = {
            @Resource(id = "first", name = "First", actions = @ResourceAction(id = "save", name = "Save")),
            @Resource(id = "second", name = "Second", actions = @ResourceAction(id = "save", name = "Save"))
        })
        public void and() { }

        @Authorize(logical = Logical.OR, resources = {
            @Resource(id = "first", name = "First", actions = @ResourceAction(id = "save", name = "Save")),
            @Resource(id = "second", name = "Second", actions = @ResourceAction(id = "save", name = "Save"))
        })
        public void or() { }

        @Authorize(resources = {
            @Resource(id = "first", name = "First", actions = @ResourceAction(id = "save", name = "Save")),
            @Resource(id = "second", name = "Second", actions = @ResourceAction(id = "save", name = "Save"))
        })
        public void inherit() { }

        @Authorize(merge = false, resources = {
            @Resource(id = "first", name = "First", actions = @ResourceAction(id = "save", name = "Save")),
            @Resource(id = "second", name = "Second", actions = @ResourceAction(id = "save", name = "Save"))
        })
        public void reset() { }

        @BothResources
        public void composed() { }
    }

    public static class DefaultController {
        @Authorize(resources = {
            @Resource(id = "first", name = "First", actions = @ResourceAction(id = "save", name = "Save")),
            @Resource(id = "second", name = "Second", actions = @ResourceAction(id = "save", name = "Save"))
        })
        public void query() { }
    }


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
