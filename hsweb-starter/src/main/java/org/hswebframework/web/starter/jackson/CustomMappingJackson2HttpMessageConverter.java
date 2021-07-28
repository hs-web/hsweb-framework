package org.hswebframework.web.starter.jackson;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.reactivestreams.Publisher;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public class CustomMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {


    private final EntityFactory entityFactory;

    public CustomMappingJackson2HttpMessageConverter(ObjectMapper objectMapper,
                                                     EntityFactory entityFactory) {
        super(objectMapper);
        this.entityFactory = entityFactory;
    }

    public Object doRead(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        if (type instanceof Class) {
            Type newType = entityFactory.getInstanceType(((Class<?>) type), false);
            if (null != newType) {
                type = newType;
            }
        }
        return super.read(type, contextClass, inputMessage);
    }

    @Override
    @Nonnull
    public Object read(@Nonnull Type type, Class<?> contextClass,@Nonnull HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {

        if (type instanceof ParameterizedType) {
            ResolvableType resolvableType = ResolvableType.forType(GenericTypeResolver.resolveType(type, contextClass));
            Class<?> clazz = resolvableType.toClass();
            //适配响应式的参数
            if (Publisher.class.isAssignableFrom(clazz)) {
                Type _gen = resolvableType.getGeneric(0).getType();
                if (Flux.class.isAssignableFrom(clazz)) {
                    //Flux则转为List
                    Object rel = doRead(ResolvableType.forClassWithGenerics(List.class,resolvableType.getGeneric(0)).getType(), contextClass, inputMessage);
                    if (rel instanceof Iterable) {
                        return Flux.fromIterable(((Iterable<?>) rel));
                    } else {
                        return Flux.just(rel);
                    }
                }
                return Mono.just(doRead(_gen, contextClass, inputMessage));
            }
        }

        return doRead(type, contextClass, inputMessage);
    }

}
