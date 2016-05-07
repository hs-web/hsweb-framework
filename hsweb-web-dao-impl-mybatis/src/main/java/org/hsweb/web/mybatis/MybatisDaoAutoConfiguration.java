package org.hsweb.web.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by zhouhao on 16-5-6.
 */
@Configuration
@ComponentScan(basePackages = {"org.hsweb.web.mybatis"})
@MapperScan(basePackages = {"org.hsweb.web.dao"})
public class MybatisDaoAutoConfiguration {

}
