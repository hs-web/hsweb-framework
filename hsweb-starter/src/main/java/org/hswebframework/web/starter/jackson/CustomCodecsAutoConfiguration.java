package org.hswebframework.web.starter.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.ClassKey;
import com.fasterxml.jackson.databind.type.ReferenceType;
import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.dict.EnumDict;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.CodecConfigurer;

import java.io.IOException;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(JacksonAutoConfiguration.class)
public class CustomCodecsAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(ObjectMapper.class)
    static class JacksonDecoderConfiguration {

        @Bean
        @Order(1)
        @ConditionalOnBean(ObjectMapper.class)
        @SuppressWarnings("all")
        CodecCustomizer jacksonDecoderCustomizer(EntityFactory entityFactory, ObjectMapper objectMapper) {
            //	objectMapper.setTypeFactory(new CustomTypeFactory(entityFactory));
            SimpleModule module = new SimpleModule();
            module.setDeserializers(new SimpleDeserializers() {

                @Override
                public JsonDeserializer<?> findBeanDeserializer(JavaType type,
                                                                DeserializationConfig config,
                                                                BeanDescription beanDesc) throws JsonMappingException {
                    JsonDeserializer<?> deserializer = super.findBeanDeserializer(type, config, beanDesc);

                    if (deserializer == null) {

                        Class clazz = entityFactory.getInstanceType(type.getRawClass(), false);

                        if (clazz == null || clazz == type.getRawClass()) {
                            return null;
                        }

                        addDeserializer((Class) type.getRawClass(), new JsonDeserializer<Object>() {
                            @Override
                            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
                                return p.readValueAs(clazz);
                            }
                        });
                    }

                    return super.findBeanDeserializer(type, config, beanDesc);
                }

                @Override
                public JsonDeserializer<?> findEnumDeserializer(Class<?> type,
                                                                DeserializationConfig config,
                                                                BeanDescription beanDesc) {
                    JsonDeserializer<?> deser = null;
                    if (type.isEnum() && EnumDict.class.isAssignableFrom(type)) {
                        deser = new EnumDict.EnumDictJSONDeserializer(val -> EnumDict
                                .find((Class) type, val)
                                .orElse(null));
                    }
                    return deser;
                }
            });
            objectMapper.registerModule(module);

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
