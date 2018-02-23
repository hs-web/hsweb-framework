package org.hswebframework.web.template.starter;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0
 */
@Configuration
@ComponentScan({"org.hswebframework.web.service.template.simple"
        , "org.hswebframework.web.controller.template"})
public class TemplateAutoConfiguration {
}
