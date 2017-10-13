package org.hswebframework.web.form;

import org.hswebframework.web.service.form.DynamicFormService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author zhouhao
 */
@Configuration
@Order
@ComponentScan({"org.hswebframework.web.service.form.simple"
        , "org.hswebframework.web.controller.form"})
public class DynamicFormAutoConfiguration implements CommandLineRunner {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DynamicFormService dynamicFormService;

    @Override
    public void run(String... args) throws Exception {
        try {
            dynamicFormService.deployAllFromLog();
        } catch (Exception e) {
            logger.error("deploy form error", e);
        }
    }
}
