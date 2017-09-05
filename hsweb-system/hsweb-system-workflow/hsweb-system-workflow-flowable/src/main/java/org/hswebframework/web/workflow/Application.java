package org.hswebframework.web.workflow;

import org.hswebframework.web.dao.Dao;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author zhouhao
 */
@SpringBootApplication
//@ComponentScan("org.hswebframework.web.workflow")
//@MapperScan(value = "org.hswebframework.web.workflow", markerInterface = Dao.class) //扫描mybatis dao
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}

