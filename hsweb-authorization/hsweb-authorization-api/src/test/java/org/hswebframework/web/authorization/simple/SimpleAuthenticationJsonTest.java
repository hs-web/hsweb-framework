package org.hswebframework.web.authorization.simple;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.hswebframework.web.authorization.builder.AuthenticationBuilder;
import org.hswebframework.web.authorization.builder.DataAccessConfigBuilder;
import org.hswebframework.web.authorization.setting.StringSourceSettingHolder;
import org.hswebframework.web.authorization.setting.UserSettingPermission;
import org.hswebframework.web.authorization.simple.builder.*;
import org.jetlinks.json.Json;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class SimpleAuthenticationJsonTest {
    @Test
    public void legacyAndNeutralArrayEntriesAgreeAndAvoidStringRoundtrip() {
        AtomicInteger passedMaps = new AtomicInteger();
        var factory = new org.hswebframework.web.authorization.builder.DataAccessConfigBuilderFactory() {
            @Override public DataAccessConfigBuilder create() {
                return new SimpleDataAccessConfigBuilder(List.of(new DataAccessConfigConverter() {
                    @Override public boolean isSupport(String type, String action, String config) { return true; }
                    @Override public org.hswebframework.web.authorization.access.DataAccessConfig convert(String type, String action, String config) {
                        assertEquals(Map.of("x", 1), Json.global().read(config, Map.class));
                        return new SimpleOwnCreatedDataAccessConfig(action);
                    }
                })) {
                    @Override public DataAccessConfigBuilder fromJson(String text) {
                        fail("已有对象不应重新编码再解析");
                        return this;
                    }
                    @Override public DataAccessConfigBuilder fromMap(Map<String, Object> value) {
                        passedMaps.incrementAndGet();
                        return super.fromMap(value);
                    }
                };
            }
        };
        String json = "[{\"id\":\"p\",\"actions\":[\"query\"],\"dataAccesses\":[{\"type\":\"test\",\"action\":\"query\",\"config\":{\"x\":1}}]}]";
        var legacy = new SimpleAuthenticationBuilder(factory);
        legacy.permission(JSON.parseArray(json));
        var neutral = new SimpleAuthenticationBuilder(factory);
        neutral.permissionJson(Json.global().readTree(json).asArray());
        assertTrue(legacy.build().hasPermission("p", List.of("query")));
        assertTrue(neutral.build().hasPermission("p", List.of("query")));
        assertEquals(2, passedMaps.get());
    }

    @Test
    public void completeAuthenticationAndSettingsUseNeutralBinding() {
        var builder = new SimpleAuthenticationBuilder(new SimpleDataAccessConfigBuilderFactory());
        var auth = builder.json("{\"user\":{\"id\":\"u\"},\"roles\":[{\"id\":\"r\"}],\"attributes\":{\"flag\":true},\"dimensions\":[{\"id\":\"d\",\"type\":\"org\"}]}" ).build();
        assertEquals("u", auth.getUser().getId());
        assertTrue(auth.hasDimension("org", "d"));
        assertEquals(Boolean.TRUE, auth.getAttribute("flag").orElseThrow());
        assertEquals(List.of(1, 2), StringSourceSettingHolder.of("[1,2]", UserSettingPermission.R).asList(Integer.class).orElseThrow());
        assertEquals(UserSettingPermission.R, StringSourceSettingHolder.of("r", UserSettingPermission.R).as(UserSettingPermission.class).orElseThrow());
    }

    @Test
    public void stringAndDocumentEntriesRetainLegacyOverrideDispatch() {
        AtomicInteger permissions = new AtomicInteger();
        AtomicInteger dimensions = new AtomicInteger();
        var builder = new SimpleAuthenticationBuilder(new SimpleDataAccessConfigBuilderFactory()) {
            @Override
            public AuthenticationBuilder permission(JSONArray array) {
                permissions.incrementAndGet();
                return super.permission(array);
            }

            @Override
            public AuthenticationBuilder dimension(JSONArray array) {
                dimensions.incrementAndGet();
                return super.dimension(array);
            }
        };
        builder.permission("[]");
        builder.json("{\"user\":{\"id\":\"u\"},\"permissions\":[],\"dimensions\":null}");
        assertEquals(2, permissions.get());
        assertEquals(1, dimensions.get());
    }

    @Test
    public void legacyOptionsRemainMutableAndPreserveInputIdentity() {
        JSONArray permissions = JSON.parseArray("[{\"id\":\"p\",\"options\":{\"x\":1}}]");
        var builder = new SimpleAuthenticationBuilder(new SimpleDataAccessConfigBuilderFactory());
        builder.permission(permissions);
        var options = builder.build().getPermission("p").orElseThrow().getOptions();
        assertSame(permissions.getJSONObject(0).getJSONObject("options"), options);
        options.put("x", 2);
        assertEquals(Integer.valueOf(2), permissions.getJSONObject(0).getJSONObject("options").getInteger("x"));
    }

    @Test
    public void existingConfigurationFactoryMethodsStillWorkOutsideSpring() {
        var configuration = new DefaultAuthorizationAutoConfiguration();
        var factory = configuration.authenticationBuilderFactory(configuration.dataAccessConfigBuilderFactory());
        assertEquals("u", factory.create().user("{\"id\":\"u\"}").build().getUser().getId());
    }

    @Test
    public void settingsRetainScalarAndEmptyValueSemantics() {
        assertTrue(StringSourceSettingHolder.of(null, UserSettingPermission.R).as(Item.class).isEmpty());
        assertTrue(StringSourceSettingHolder.of("   ", UserSettingPermission.R).asList(Item.class).isEmpty());
        assertTrue(StringSourceSettingHolder.of("null", UserSettingPermission.R).asList(Item.class).isEmpty());
        assertTrue(StringSourceSettingHolder.of("null", UserSettingPermission.R).as(Item.class).isEmpty());
        assertEquals("plain text", StringSourceSettingHolder.of("plain text", UserSettingPermission.R)
            .as(String.class).orElseThrow());
        assertEquals(Integer.valueOf(42), StringSourceSettingHolder.of("42", UserSettingPermission.R)
            .as(Integer.class).orElseThrow());
        assertTrue(StringSourceSettingHolder.of("true", UserSettingPermission.R).as(Boolean.class).orElseThrow());
        assertEquals("中文😀", StringSourceSettingHolder.of("[{\"name\":\"中文😀\"}]", UserSettingPermission.R)
            .asList(Item.class).orElseThrow().get(0).name);
    }

    @Test
    public void typedMapReadsRetainSerializableAttributesAndPrecision() {
        var builder = new SimpleAuthenticationBuilder(new SimpleDataAccessConfigBuilderFactory());
        var auth = builder.attributes("{\"items\":[{\"name\":\"中文😀\"}],\"flag\":true,"
            + "\"large\":1234567890123456789012345,\"decimal\":1.0000000000000000001,\"missing\":null}").build();
        assertEquals(List.of(Map.of("name", "中文😀")), auth.getAttributes().get("items"));
        assertEquals(Boolean.TRUE, auth.getAttributes().get("flag"));
        assertEquals(new BigInteger("1234567890123456789012345"), auth.getAttributes().get("large"));
        assertEquals(new BigDecimal("1.0000000000000000001"), auth.getAttributes().get("decimal"));
        assertTrue(auth.getAttributes().containsKey("missing"));
        assertNull(auth.getAttributes().get("missing"));
    }

    @Test
    public void jsonConfigEntryRetainsConverterPayload() {
        AtomicInteger calls = new AtomicInteger();
        DataAccessConfigConverter converter = new DataAccessConfigConverter() {
            @Override public boolean isSupport(String type, String action, String config) {
                return "custom".equals(type) && "query".equals(action);
            }
            @Override public org.hswebframework.web.authorization.access.DataAccessConfig convert(String type, String action, String config) {
                assertEquals(Map.of("rows", List.of(Map.of("number", new BigDecimal("1.0000000000000000001")))),
                    Json.global().readAsMap(config, Object.class));
                calls.incrementAndGet();
                return new SimpleOwnCreatedDataAccessConfig(action);
            }
        };
        var config = new SimpleDataAccessConfigBuilder(List.of(converter))
            .fromJson("{\"type\":\"custom\",\"action\":\"query\",\"config\":{\"rows\":[{\"number\":1.0000000000000000001}]}}")
            .build();
        assertNotNull(config);
        assertEquals(1, calls.get());
    }

    public static class Item {
        public String name;
    }
}
