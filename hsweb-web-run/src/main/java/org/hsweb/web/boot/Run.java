package org.hsweb.web.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.support.RequestContext;

/**
 * Created by 浩 on 2016-01-16 0016.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"org.hsweb.web"})
@MapperScan(basePackages = {"org.hsweb.web.dao"})
@EnableTransactionManagement(proxyTargetClass = true)
public class Run {
    public static void main(String[] args) {
        SpringApplication.run(Run.class,args);
    }
}
