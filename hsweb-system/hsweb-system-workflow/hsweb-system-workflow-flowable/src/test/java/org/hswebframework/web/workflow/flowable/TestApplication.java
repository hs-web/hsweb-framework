package org.hswebframework.web.workflow.flowable;

import org.hswebframework.web.service.organizational.RelationDefineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@SpringBootApplication
public class TestApplication {
    @Autowired
    RelationDefineService relationDefineService;

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
