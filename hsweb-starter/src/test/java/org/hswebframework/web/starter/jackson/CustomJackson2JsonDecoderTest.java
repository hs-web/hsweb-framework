package org.hswebframework.web.starter.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.crud.entity.factory.MapperEntityFactory;
import org.hswebframework.web.crud.web.reactive.ReactiveQueryController;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CustomJackson2JsonDecoderTest {

    @Test
    @SneakyThrows
    public void testDecodeCustomType() {

        MapperEntityFactory entityFactory = new MapperEntityFactory();

        entityFactory.addMapping(QueryParamEntity.class, MapperEntityFactory.defaultMapper(CustomQueryParamEntity.class));


        ObjectMapper mapper = new ObjectMapper();
        CustomJackson2JsonDecoder decoder = new CustomJackson2JsonDecoder(entityFactory, mapper);

        ResolvableType type = ResolvableType.forMethodParameter(
                ReactiveQueryController.class.getMethod("query", QueryParamEntity.class), 0
        );

        DataBuffer buffer = new DefaultDataBufferFactory().wrap("{}".getBytes());

        Object object = decoder.decode(buffer, type, MediaType.APPLICATION_JSON, Collections.emptyMap());

        assertTrue(object instanceof CustomQueryParamEntity);

    }

    @Test
    @SneakyThrows
    public void testDecodeList() {
        ObjectMapper mapper = new ObjectMapper();
        CustomJackson2JsonDecoder decoder = new CustomJackson2JsonDecoder(new MapperEntityFactory(), mapper);

        ResolvableType type = ResolvableType.forClassWithGenerics(List.class, MyEntity.class);
        DataBuffer buffer = new DefaultDataBufferFactory().wrap("[{\"id\":\"test\"}]".getBytes());

        Object object = decoder.decode(buffer, type, MediaType.APPLICATION_JSON, Collections.emptyMap());

        assertTrue(object instanceof List);
        assertTrue(((List<?>) object).size() > 0);
        assertTrue(((List<?>) object).get(0) instanceof MyEntity);
        assertEquals(((MyEntity) ((List<?>) object).get(0)).getId(), "test");

    }

    @Test
    @SneakyThrows
    public void testGeneric() {
        ObjectMapper mapper = new ObjectMapper();
        CustomJackson2JsonDecoder decoder = new CustomJackson2JsonDecoder(new MapperEntityFactory(), mapper);

        ResolvableType type = ResolvableType.forClassWithGenerics(PagerResult.class, MyEntity.class);
        DataBuffer buffer = new DefaultDataBufferFactory().wrap("{\"pageSize\":1,\"data\":[{\"id\":\"test\"}]}".getBytes());

        Object object = decoder.decode(buffer, type, MediaType.APPLICATION_JSON, Collections.emptyMap());

        assertTrue(object instanceof PagerResult);
        PagerResult<MyEntity> result= ((PagerResult<MyEntity>) object);

        assertTrue(result.getData().size()>0);
        assertEquals(result.getData().get(0).getId(), "test");

    }

    @Test
    @SneakyThrows
    public void testComplexGeneric() {
        ObjectMapper mapper = new ObjectMapper();
        CustomJackson2JsonDecoder decoder = new CustomJackson2JsonDecoder(new MapperEntityFactory(), mapper);
        ResolvableType type = ResolvableType.forClassWithGenerics(PagerResult.class, ResolvableType.forClassWithGenerics(
                Map.class,String.class,MyEntity.class
        ));
        DataBuffer buffer = new DefaultDataBufferFactory().wrap("{\"pageSize\":1,\"data\":[{\"test\":{\"id\":\"test\"}}]}".getBytes());

        Object object = decoder.decode(buffer, type, MediaType.APPLICATION_JSON, Collections.emptyMap());

        assertTrue(object instanceof PagerResult);
        PagerResult<Map<String,MyEntity>> result= ((PagerResult<Map<String,MyEntity>>) object);

        assertTrue(result.getData().size()>0);
        assertEquals(result.getData().get(0).get("test").getId(), "test");
    }


    @Getter
    @Setter
    public static class MyEntity {
        private String id;
    }

    public static class CustomQueryParamEntity extends QueryParamEntity {

    }

}