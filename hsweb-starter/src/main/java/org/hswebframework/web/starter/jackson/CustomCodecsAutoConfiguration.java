package org.hswebframework.web.starter.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.CodecConfigurer;

import java.io.IOException;

@AutoConfiguration(after = JacksonAutoConfiguration.class)
public class CustomCodecsAutoConfiguration {

    @AutoConfiguration
    @ConditionalOnClass(ObjectMapper.class)
    static class JacksonDecoderConfiguration {

        @Bean
        SimpleModule springWebModule() {
            //兼容spring web相关序列化
            SimpleModule module = new SimpleModule();
            module.addSerializer(HttpMethod.class, new JsonSerializer<>() {
                @Override
                public void serialize(HttpMethod httpMethod, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                    jsonGenerator.writeString(httpMethod.name());
                }
            });
            module.addDeserializer(HttpMethod.class, new JsonDeserializer<>() {
                @Override
                public HttpMethod deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                    return HttpMethod.valueOf(jsonParser.getValueAsString());
                }
            });
            module.addSerializer(HttpStatus.class, new JsonSerializer<>() {
                @Override
                public void serialize(HttpStatus httpStatus, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                    jsonGenerator.writeNumber(httpStatus.value());
                }
            });
            module.addDeserializer(HttpStatus.class, new JsonDeserializer<>() {
                @Override
                public HttpStatus deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                    return HttpStatus.valueOf(jsonParser.getValueAsInt());
                }
            });
            return module;
        }

        @Bean
        SimpleModule entityAndEnumDictModule(EntityFactory entityFactory) {
            SimpleModule module = new SimpleModule();
            module.setDeserializers(new CustomDeserializers(entityFactory));
            return module;
        }

        @Bean
        @Order(1)
        @ConditionalOnBean(ObjectMapper.class)
        @SuppressWarnings("all")
        CodecCustomizer jacksonDecoderCustomizer(EntityFactory entityFactory, ObjectMapper objectMapper) {
            return (configurer) -> {
                CodecConfigurer.DefaultCodecs defaults = configurer.defaultCodecs();
                defaults.jackson2JsonDecoder(new CustomJackson2JsonDecoder(entityFactory, objectMapper));
                defaults.jackson2JsonEncoder(new CustomJackson2jsonEncoder(objectMapper));
            };
        }

        @Bean
        CustomMappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(EntityFactory entityFactory, ObjectMapper objectMapper) {
            return new CustomMappingJackson2HttpMessageConverter(objectMapper, entityFactory);
        }

    }


}
