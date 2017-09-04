package org.hswebframework.web.workflow;

import org.hswebframework.web.dao.Dao;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.Collections;
import java.util.List;

/**
 * @author zhouhao
 */
@SpringBootApplication
@ComponentScan("org.hswebframework.web.workflow")
@MapperScan(value = "org.hswebframework.web.workflow", markerInterface = Dao.class) //扫描mybatis dao
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}

