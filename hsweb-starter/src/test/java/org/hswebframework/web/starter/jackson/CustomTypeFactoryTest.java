package org.hswebframework.web.starter.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomTypeFactoryTest {

    private ObjectMapper mapper;

    @Before
    public void init() {
        MapperEntityFactory entityFactory = new MapperEntityFactory();
        entityFactory.addMapping(TestEntity.class, MapperEntityFactory.defaultMapper(JpaTestEntity.class));

        CustomTypeFactory factory = new CustomTypeFactory(entityFactory);

        mapper = new ObjectMapper()
                .setTypeFactory(factory);
    }

    @Test
    @SneakyThrows
    public void testSimple() {
        TestEntity entity = mapper.readValue("{\"name\":\"test\"}", TestEntity.class);

        Assert.assertTrue(entity instanceof JpaTestEntity);
        Assert.assertEquals(entity.getName(), "test");
    }

    @Test
    @SneakyThrows
    public void testList() {
        List<TestEntity> entity = mapper.readValue("[{\"name\":\"test\"}]", mapper.getTypeFactory().constructCollectionType(ArrayList.class, TestEntity.class));

        Assert.assertTrue(entity instanceof ArrayList);
        Assert.assertFalse(entity.isEmpty());

        Assert.assertEquals(entity.get(0).getName(), "test");
    }

    @Test
    @SneakyThrows
    public void testMap() {
        Map<String,TestEntity>entity = mapper.readValue("{\"info\":{\"name\":\"test\"}}", mapper.getTypeFactory().constructMapType(HashMap.class,String.class, TestEntity.class));

        Assert.assertTrue(entity instanceof HashMap);
        Assert.assertFalse(entity.isEmpty());

        Assert.assertEquals(entity.get("info").getName(), "test");
    }

    @Getter
    @Setter
    public static class JpaTestEntity implements TestEntity {

        private String name;

    }

    public interface TestEntity {
        String getName();

    }

}