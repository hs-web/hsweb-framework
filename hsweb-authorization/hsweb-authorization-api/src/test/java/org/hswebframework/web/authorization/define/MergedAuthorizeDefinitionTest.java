package org.hswebframework.web.authorization.define;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.*;

public class MergedAuthorizeDefinitionTest {

    @Test
    public void test() {
        MergedAuthorizeDefinition definition = new MergedAuthorizeDefinition();
        definition.addResource(ResourceDefinition.of("test", "测试").addAction("create", "新增"));
        definition.addResource(ResourceDefinition.of("test", "测试").addAction("update", "修改"));
        definition.addResource(ResourceDefinition.of("test", "测试").addAction("update", "修改"));


        Set<ResourceDefinition> definitions = definition.getResources();
        Assert.assertEquals(definitions.size(), 1);
        Assert.assertTrue(definitions.iterator().next().hasAction(Arrays.asList("create")));
        Assert.assertTrue(definitions.iterator().next().hasAction(Arrays.asList("update")));

    }

}