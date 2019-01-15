/*
 *
 *  * Copyright 2019 http://www.hswebframework.org
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

package org.hswebframework.web.dao.mybatis;

import org.hswebframework.ezorm.rdb.render.dialect.Dialect;
import org.hswebframework.web.dao.Dao;
import org.hswebframework.web.dao.mybatis.mapper.SqlTermCustomizer;
import org.hswebframework.web.dao.mybatis.mapper.dict.DictInTermTypeMapper;
import org.hswebframework.web.dao.mybatis.mapper.dict.DictTermTypeMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@ComponentScan("org.hswebframework.web.dao.mybatis")
@MapperScan(value = "org.hswebframework.web.dao"
        , markerInterface = Dao.class
        , sqlSessionFactoryRef = "sqlSessionFactory")
@AutoConfigureAfter(MyBatisAutoConfiguration.class)
@EnableConfigurationProperties(MybatisProperties.class)
public class MybatisDaoAutoConfiguration {
    @Bean
    public DictTermTypeMapper dictTermTypeMapper() {
        return new DictTermTypeMapper(false);
    }

    @Bean
    public DictTermTypeMapper dictNotTermTypeMapper() {
        return new DictTermTypeMapper(true);
    }

    @Bean
    public DictInTermTypeMapper dictInTermTypeMapper() {
        return new DictInTermTypeMapper(false);
    }

    @Bean
    public DictInTermTypeMapper dictNotInTermTypeMapper() {
        return new DictInTermTypeMapper(true);
    }

    @Bean
    public BeanPostProcessor sqlTermCustomizerRegister() {

        List<Dialect> dialects = Arrays.asList(
                Dialect.H2
                , Dialect.MYSQL
                , Dialect.ORACLE
                , Dialect.POSTGRES
                , Dialect.MSSQL);

        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof SqlTermCustomizer) {
                    SqlTermCustomizer customizer = ((SqlTermCustomizer) bean);
                    if (customizer.forDialect() != null) {
                        for (Dialect dialect : customizer.forDialect()) {
                            dialect.setTermTypeMapper(customizer.getTermType(), customizer);
                        }
                    } else {
                        dialects.forEach(dialect -> dialect.setTermTypeMapper(customizer.getTermType(), customizer));
                    }
                }
                return bean;
            }
        };
    }
}
