package org.hswebframework.web.dict;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class EnumDictTest {

    @Test
    @SneakyThrows
    public void testJackson() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        JsonDeserializer<EnumDict> deserialize = new EnumDict.EnumDictJSONDeserializer();
        module.addDeserializer(Enum.class, (JsonDeserializer) deserialize);
        mapper.registerModule(module);


        String val = mapper.writer().writeValueAsString(new TestEntity());

        System.out.println(val);
        TestEntity testEntity = mapper.readerFor(TestEntity.class)
                .readValue(val);

        Assert.assertEquals(testEntity.testEnum, TestEnum.E1);
        testEntity = mapper.readerFor(TestEntity.class)
                .readValue("{\"testEnum\":\"E1\"}");
        Assert.assertEquals(testEntity.testEnum, TestEnum.E1);

        testEntity = mapper.readerFor(TestEntity.class)
                .readValue("{\"testEnum\":\"e1\"}");
        Assert.assertEquals(testEntity.testEnum, TestEnum.E1);

        System.out.println((Object) mapper.readerFor(TestEnum.class).readValue("\"E1\""));

    }

    @Getter
    @Setter
    public static class TestEntity {
        private TestEnum testEnum = TestEnum.E1;

        private SimpleEnum simpleEnum = SimpleEnum.A;
    }

    public  enum SimpleEnum{
        A,B
    }

}