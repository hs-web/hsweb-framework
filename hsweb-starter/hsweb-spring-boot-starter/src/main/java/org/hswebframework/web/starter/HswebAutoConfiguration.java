/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.starter;

import com.alibaba.fastjson.serializer.SerializerFeature;
import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.authorization.AuthenticationSupplier;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.commons.entity.factory.MapperEntityFactory;
import org.hswebframework.web.starter.convert.FastJsonHttpMessageConverter;
import org.hswebframework.web.starter.resolver.AuthorizationArgumentResolver;
import org.hswebframework.web.starter.resolver.JsonParamResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
@ComponentScan("org.hswebframework.web")
@EnableConfigurationProperties(EntityProperties.class)
public class HswebAutoConfiguration {

    @Autowired
    private EntityProperties entityProperties;

    @Bean
    @Primary
    public FastJsonHttpMessageConverter fastJsonHttpMessageConverter(@Autowired(required = false) EntityFactory entityFactory) {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        // TODO: 16-12-24  应该可配置
        converter.setFeatures(
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.WriteNullBooleanAsFalse
//                SerializerFeature.WriteDateUseDateFormat
        );
        converter.setEntityFactory(entityFactory);
        return converter;
    }

    @Bean
    public JsonParamResolver jsonParamResolver(FastJsonHttpMessageConverter fastJsonHttpMessageConverter) {
        return new JsonParamResolver(fastJsonHttpMessageConverter);
    }

    @Bean
    public AuthorizationArgumentResolver authorizationArgumentResolver() {
        return new AuthorizationArgumentResolver();
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(List<HandlerMethodArgumentResolver> handlerMethodArgumentResolvers) {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
                super.addArgumentResolvers(argumentResolvers);
                argumentResolvers.addAll(handlerMethodArgumentResolvers);
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new HandlerInterceptorAdapter() {
                    @Override
                    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
                        //clear thread local
                        ThreadLocalUtils.clear();
                    }
                });
            }
        };
    }

    @Bean(name = "validator")
    @ConditionalOnMissingBean(Validator.class)
    public Validator validator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        return validator;
    }

    @Bean(name = "entityFactory")
    @ConditionalOnMissingBean(EntityFactory.class)
    public EntityFactory entityFactory() {
        return new MapperEntityFactory(entityProperties.createMappers());
    }

}
