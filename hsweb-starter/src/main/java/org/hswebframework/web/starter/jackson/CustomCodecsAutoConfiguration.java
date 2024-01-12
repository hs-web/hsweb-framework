package org.hswebframework.web.starter.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.CodecConfigurer;

@AutoConfiguration(after = JacksonAutoConfiguration.class)
public class CustomCodecsAutoConfiguration {

    @AutoConfiguration
    @ConditionalOnClass(ObjectMapper.class)
    static class JacksonDecoderConfiguration {


        @Bean
        SimpleModule entityAndEnumDictModule() {
            SimpleModule module = new SimpleModule();
            module.setDeserializers(new CustomDeserializers());
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
