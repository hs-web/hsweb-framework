package org.hswebframework.web.starter.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.authorization.builder.AuthenticationBuilderFactory;
import org.hswebframework.web.authorization.simple.DefaultAuthorizationAutoConfiguration;
import org.jetlinks.json.Json;
import org.jetlinks.json.JsonMapper;
import org.jetlinks.json.spring.JsonFacadeAutoConfiguration;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.Assert.*;

public class JsonFacadeMapperIntegrationTest {
    private ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
            .withPropertyValues("json.facade.global-enabled=false")
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class,
                JsonFacadeAutoConfiguration.class,
                DefaultAuthorizationAutoConfiguration.class,
                CustomCodecsAutoConfiguration.JacksonDecoderConfiguration.class))
            .withBean(EntityFactory.class, () -> new MapperEntityFactory().addMapping(Named.class, NamedValue::new));
    }

    @Test
    public void configuredSpringMapperIsAdaptedAcrossIndependentContexts() {
        for (int i = 0; i < 2; i++) {
            runner().withPropertyValues("spring.jackson.property-naming-strategy=SNAKE_CASE")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    JsonMapper mapper = context.getBean(JsonMapper.class);
                    assertEquals("{\"device_id\":\"test\"}", mapper.write(new Item("test")));
                    assertEquals("test", mapper.read("{\"device_id\":\"test\"}", Item.class).deviceId());
                    assertEquals("test", mapper.readAsList("[{\"device_id\":\"test\"}]", Item.class)
                        .get(0).deviceId());
                    assertEquals("test", mapper.readAsMap("{\"first\":{\"device_id\":\"test\"}}", Item.class)
                        .get("first").deviceId());
                    assertEquals("sensor", context.getBean(AuthenticationBuilderFactory.class).create()
                        .user("{\"id\":\"u\",\"user_type\":\"sensor\"}").build().getUser().getUserType());
                    assertTrue(context.getBean(ObjectMapper.class).getFactory().isEnabled(com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE));
                });
        }
    }

    @Test
    public void suppliedFacadeTakesPrecedence() {
        JsonMapper supplied = Json.builder().provider("jackson2").build();
        runner().withBean(JsonMapper.class, () -> supplied)
            .run(context -> assertSame(supplied, context.getBean(JsonMapper.class)));
    }

    @Test
    public void nestedEntityMappingsUseContextBindingAndRetainRootValidation() {
        runner().run(context -> {
            JsonMapper mapper = context.getBean(JsonMapper.class);
            Envelope value = mapper.read("{\"values\":[{\"deviceId\":\"one\"},{\"deviceId\":\"two\"}],\"tail\":true}", Envelope.class);
            assertEquals("two", value.values().get(1).getDeviceId());
            assertTrue(value.tail());
            assertThrows(org.jetlinks.json.JsonException.class,
                () -> mapper.read("{\"values\":[],\"tail\":true} {}", Envelope.class));
        });
    }

    @Test
    public void dictionaryBindingUsesExistingApplicationModule() {
        runner().run(context -> {
            JsonMapper mapper = context.getBean(JsonMapper.class);
            assertEquals(Choice.ONE, mapper.read("\"one\"", Choice.class));
            assertEquals(Choice.ONE, mapper.read("{\"value\":\"one\"}", Choice.class));
            assertEquals(Choice.ONE, mapper.read("[\"one\"]", Choice[].class)[0]);
            assertEquals("\"one\"", mapper.write(Choice.ONE));
        });
    }

    public enum Choice implements EnumDict<String> {
        ONE;
        @Override public String getValue() { return "one"; }
        @Override public String getText() { return "One"; }
        @Override public boolean isWriteJSONObjectEnabled() { return false; }
    }
    public record Item(String deviceId) { }
    public record Envelope(java.util.List<Named> values, boolean tail) { }
    public interface Named { String getDeviceId(); }
    public static class NamedValue implements Named {
        public String deviceId;
        @Override public String getDeviceId() { return deviceId; }
    }
}
