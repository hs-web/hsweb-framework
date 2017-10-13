package org.hswebframework.web.template.starter;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since
 */
@Configuration
@ComponentScan({"org.hswebframework.web.service.template.simple"
        , "org.hswebframework.web.controller.template"})
public class TemplateAutoConfiguration {
}
