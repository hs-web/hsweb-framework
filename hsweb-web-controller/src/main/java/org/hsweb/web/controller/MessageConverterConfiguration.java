package org.hsweb.web.controller;

import com.alibaba.fastjson.serializer.SerializerFeature;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.message.FastJsonHttpMessageConverter;
import org.hsweb.web.core.message.ResponseMessage;
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

    public static void main(String[] args) {
        User user = new User();
        user.setPassword("test");
        user.setUsername("test1");
        ResponseMessage message = ResponseMessage.created(user)
                .exclude("password");
        System.out.println(new FastJsonHttpMessageConverter().converter(message));
    }
}
