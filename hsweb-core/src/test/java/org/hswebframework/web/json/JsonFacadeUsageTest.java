package org.hswebframework.web.json;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hswebframework.web.bean.ToString;
import org.hswebframework.web.dict.EnumDict;
import org.jetlinks.json.Json;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;

public class JsonFacadeUsageTest {
    @Test
    public void enumAndLegacyOutputOverrideRemainCompatible() {
        var mapper = Json.global();
        assertSame(mapper, Json.global());
        assertEquals("{\"custom\":\"one\"}", mapper.write(Choice.ONE));
        assertEquals(mapper.write(Choice.ONE), JSON.toJSONString(Choice.ONE));
    }

    @Test
    public void nativeWriteOnlyAnnotationsAreRetained() {
        var mapper = Json.global();
        Secret secret = mapper.read("{\"name\":\"test\",\"password\":\"hidden\"}", Secret.class);
        assertEquals("hidden", secret.password);
        assertEquals("{\"name\":\"test\"}", mapper.write(secret));
    }

    @Test
    public void datePrecisionAndTypeMetadataUseHostConfiguration() {
        var mapper = Json.global();
        assertEquals("0", mapper.write(new Date(0)));
        assertEquals(new Date(0), mapper.read("0", Date.class));
        assertEquals(new BigDecimal("12345678901234567890.123456789"),
            mapper.read("12345678901234567890.123456789", BigDecimal.class));
        Object metadata = mapper.read("{\"@type\":\"java.lang.Thread\",\"value\":1}", Object.class);
        assertTrue(metadata instanceof Map);
        assertEquals("java.lang.Thread", ((Map<?, ?>) metadata).get("@type"));
    }

    @Test
    public void jsonLoggingRetainsSensitiveFieldMasking() {
        long features = ToString.Feature.createFeatures(ToString.Feature.jsonFormat, ToString.Feature.coverIgnoreProperty);
        String json = ToString.getOperator(LogItem.class).toString(new LogItem(), features, java.util.Set.of());
        assertFalse(json.contains("raw-secret"));
        assertEquals("test", Json.global().read(json, Map.class).get("name"));
    }

    public static class LogItem {
        @ToString.Ignore
        private String password = "raw-secret";
        private String name = "test";
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public enum Choice implements EnumDict<String> {
        ONE;
        @Override public String getValue() { return "one"; }
        @Override public String getText() { return "One"; }
        @Override public Object getWriteJSONObject() { return java.util.Map.of("custom", getValue()); }
    }

    public static class Secret {
        public String name;
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        public String password;
    }
}
