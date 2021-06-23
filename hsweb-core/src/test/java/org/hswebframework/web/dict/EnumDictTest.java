package org.hswebframework.web.dict;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.ClassKey;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class EnumDictTest {

    @Test
    @SneakyThrows
    public void testJackson() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.setDeserializers(new SimpleDeserializers() {
            @Override
            public JsonDeserializer<?> findEnumDeserializer(Class<?> type,
                                                            DeserializationConfig config,
                                                            BeanDescription beanDesc) throws JsonMappingException {
                JsonDeserializer<?> deser = null;
                if (type.isEnum()) {
                    if (EnumDict.class.isAssignableFrom(type)) {
                        deser = new EnumDict.EnumDictJSONDeserializer(val -> EnumDict
                                .find((Class) type, val)
                                .orElse(null));
                    }
                }
                return deser;
            }
        });
        mapper.registerModule(module);


//        String val = mapper.writer().writeValueAsString(new TestEntity());
//
//        System.out.println(val);
//        TestEntity testEntity = mapper.readerFor(TestEntity.class)
//                                      .readValue(val);
//
//        Assert.assertEquals(testEntity.testEnum, TestEnum.E1);
//        testEntity = mapper.readerFor(TestEntity.class)
//                           .readValue("{\"testEnum\":\"E1\"}");
//        Assert.assertEquals(testEntity.testEnum, TestEnum.E1);
//
//        testEntity = mapper.readerFor(TestEntity.class)
//                           .readValue("{\"testEnum\":\"e1\"}");
//        Assert.assertEquals(testEntity.testEnum, TestEnum.E1);
//
//        System.out.println((Object) mapper.readerFor(TestEnum.class).readValue("\"E1\""));

        TestEntity testEntity = mapper.readerFor(TestEntity.class)
                                      .readValue("{\"testEnums\":[\"E1\"]}");
        System.out.println(testEntity.getTestEnums());
//        Assert.assertArrayEquals(testEntity.getSimpleEnums(), new TestEnum[]{TestEnum.E1});

    }

    @Test
    public void testEq() {
        assertFalse(EnumDict.find(TestEnum.class, 1)
                            .isPresent());

        assertTrue(EnumDict.find(TestEnum.class, "e1")
                           .isPresent());

        assertTrue(EnumDict.find(TestEnum.class, "E1")
                           .isPresent());


    }

    @Getter
    @Setter
    public static class TestEntity {
        private TestEnum testEnum = TestEnum.E1;

        private SimpleEnum simpleEnum = SimpleEnum.A;

        private TestEnum[] testEnums;
    }

    public enum SimpleEnum {
        A, B
    }

}