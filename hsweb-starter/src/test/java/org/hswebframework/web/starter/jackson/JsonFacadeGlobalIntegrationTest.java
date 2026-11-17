package org.hswebframework.web.starter.jackson;

import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.authorization.setting.StringSourceSettingHolder;
import org.hswebframework.web.authorization.setting.UserSettingPermission;
import org.hswebframework.web.authorization.simple.builder.SimpleAuthenticationBuilder;
import org.hswebframework.web.authorization.simple.builder.SimpleDataAccessConfigBuilderFactory;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.hswebframework.web.utils.ModuleUtils;
import org.jetlinks.json.Json;
import org.jetlinks.json.JsonMapper;
import org.jetlinks.json.spring.JsonFacadeAutoConfiguration;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.Assert.*;

public class JsonFacadeGlobalIntegrationTest {
    @Test
    public void metadataBeforeSpringDoesNotReserveGlobalAndDefaultEntriesUseSpringConfiguration() {
        assertEquals("metadata-before-spring", ModuleUtils.getModule("json-facade-test").getComment());
        new ApplicationContextRunner()
            .withPropertyValues("spring.jackson.property-naming-strategy=KEBAB_CASE")
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class,
                JsonFacadeAutoConfiguration.class,
                CustomCodecsAutoConfiguration.JacksonDecoderConfiguration.class))
            .withBean(EntityFactory.class, MapperEntityFactory::new)
            .run(context -> {
                assertNull(context.getStartupFailure());
                assertSame(context.getBean(JsonMapper.class), Json.global());
                var builder = new SimpleAuthenticationBuilder(new SimpleDataAccessConfigBuilderFactory());
                assertEquals("sensor", builder.user("{\"id\":\"u\",\"user-type\":\"sensor\"}")
                    .build().getUser().getUserType());
                var setting = StringSourceSettingHolder.of("{\"device-id\":\"test\"}", UserSettingPermission.R);
                assertEquals("test", setting.as(Item.class).orElseThrow().deviceId());
            });
    }

    public record Item(String deviceId) { }
}
