package org.hsweb.web.controller;

import com.alibaba.fastjson.serializer.SerializerFeature;
import org.hsweb.web.core.message.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * Created by 浩 on 2016-01-22 0022.
 */
@Configuration
public class MessageConverterConfiguration {

    @Bean
    public HttpMessageConverter<Object> converter() {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        converter.setFeatures(
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.WriteDateUseDateFormat
        );
        return converter;
    }
}
