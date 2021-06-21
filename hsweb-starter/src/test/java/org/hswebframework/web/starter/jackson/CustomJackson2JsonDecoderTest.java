package org.hswebframework.web.starter.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.hswebframework.web.api.crud.entity.EntityFactory;
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
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.junit.Assert.*;

public class CustomJackson2JsonDecoderTest {

    @Test
    @SneakyThrows
    public void testDecodeCustomType() {

        MapperEntityFactory entityFactory = new MapperEntityFactory();

        entityFactory.addMapping(QueryParamEntity.class,MapperEntityFactory.defaultMapper(CustomQueryParamEntity.class));


        ObjectMapper mapper = new ObjectMapper();
        CustomJackson2JsonDecoder decoder = new CustomJackson2JsonDecoder(entityFactory, mapper);

        ResolvableType type = ResolvableType.forMethodParameter(
                ReactiveQueryController.class.getMethod("query", QueryParamEntity.class), 0
        );

        DataBuffer buffer = new DefaultDataBufferFactory().wrap("{}".getBytes());

        Object object = decoder.decode(buffer, type, MediaType.APPLICATION_JSON, Collections.emptyMap());

        assertTrue(object instanceof CustomQueryParamEntity);

    }

    public static class CustomQueryParamEntity extends QueryParamEntity {

    }

}