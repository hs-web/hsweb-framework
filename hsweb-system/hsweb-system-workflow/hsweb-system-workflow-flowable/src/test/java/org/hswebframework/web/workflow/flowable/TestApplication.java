package org.hswebframework.web.workflow.flowable;

import org.hswebframework.web.service.organizational.RelationDefineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@SpringBootApplication
@ActiveProfiles("dev")
public class TestApplication {
    @Autowired
    RelationDefineService relationDefineService;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(TestApplication.class);
        application.setAdditionalProfiles("dev");
        application.run(args);
    }
}
