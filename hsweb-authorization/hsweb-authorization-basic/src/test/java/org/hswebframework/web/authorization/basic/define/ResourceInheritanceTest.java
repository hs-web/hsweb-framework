package org.hswebframework.web.authorization.basic.define;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.Dimension;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.ResourceAction;
import org.hswebframework.web.authorization.define.AopAuthorizeDefinition;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

import static org.junit.Assert.*;

public class ResourceInheritanceTest {

    @Test
    public void replacementWithAnotherIdKeepsOnlyTheMethodResourceAndQueryAction() throws Exception {
        AopAuthorizeDefinition definition = parse("replace");
        assertEquals(1, definition.getResources().getResources().size());
        assertEquals(Set.of("query"), definition.getResources().getResource("channel").orElseThrow().getActionIds());
        assertFalse(definition.getResources().getResource("collector").isPresent());
        assertEquals(1, definition.getDimensions().getDimensions().size());
    }

    @Test
    public void replacementWithTheSameIdDiscardsInheritedActions() throws Exception {
        AopAuthorizeDefinition definition = parse("replaceSameId");
        assertEquals(1, definition.getResources().getResources().size());
        assertEquals(Set.of("query"), definition.getResources().getResource("collector").orElseThrow().getActionIds());
    }

    @Test
    public void defaultMergeKeepsInheritedResources() throws Exception {
        AopAuthorizeDefinition definition = parse("merge");
        assertEquals(2, definition.getResources().getResources().size());
        assertEquals(Set.of("save"), definition.getResources().getResource("collector").orElseThrow().getActionIds());
        assertEquals(Set.of("query"), definition.getResources().getResource("channel").orElseThrow().getActionIds());
    }

    @Test
    public void inheritedResourceReceivesMethodAction() throws Exception {
        assertEquals(Set.of("save", "query"), parse("inherit").getResources()
            .getResource("collector").orElseThrow().getActionIds());
    }

    @Test
    public void replacementKeepsEveryMethodResource() throws Exception {
        AopAuthorizeDefinition definition = parse("replaceMultiple");
        assertEquals(2, definition.getResources().getResources().size());
        assertFalse(definition.getResources().getResource("collector").isPresent());
        for (String id : Set.of("channel", "point")) {
            assertEquals(Set.of("query"), definition.getResources().getResource(id).orElseThrow().getActionIds());
        }
    }

    @Test
    public void composedResourceCanReplaceInheritedDeclaration() throws Exception {
        AopAuthorizeDefinition definition = parse("composed");
        assertEquals(1, definition.getResources().getResources().size());
        assertEquals(Set.of("query"), definition.getResources().getResource("channel").orElseThrow().getActionIds());
    }

    @Test
    public void explicitAuthorizeResetRunsBeforeMethodResourceAndAction() throws Exception {
        AopAuthorizeDefinition definition = parse("reset");
        assertEquals(1, definition.getResources().getResources().size());
        assertEquals(Set.of("query"), definition.getResources().getResource("channel").orElseThrow().getActionIds());
        assertTrue(definition.getDimensions().isEmpty());
    }

    private AopAuthorizeDefinition parse(String method) throws Exception {
        return DefaultBasicAuthorizeDefinition.from(Controller.class, Controller.class.getMethod(method));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Resource(id = "channel", name = "Channel", merge = false)
    public @interface ChannelResource { }

    @Retention(RetentionPolicy.RUNTIME)
    @Resource(id = "channel", name = "Channel", merge = false)
    @Resource(id = "point", name = "Point")
    public @interface MethodResources { }

    @Resource(id = "collector", name = "Collector", actions = @ResourceAction(id = "save", name = "Save"))
    @Authorize(dimension = @Dimension(type = "organization", id = "organization"))
    public static class Controller {
        @Resource(id = "channel", name = "Channel", merge = false)
        @QueryAction
        public void replace() { }

        @Resource(id = "collector", name = "Collector", merge = false)
        @QueryAction
        public void replaceSameId() { }

        @Resource(id = "channel", name = "Channel")
        @QueryAction
        public void merge() { }

        @QueryAction
        public void inherit() { }

        @MethodResources
        @QueryAction
        public void replaceMultiple() { }

        @ChannelResource
        @QueryAction
        public void composed() { }

        @Authorize(merge = false)
        @Resource(id = "channel", name = "Channel")
        @QueryAction
        public void reset() { }
    }
}
