package org.hswebframework.web.file.starter;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 * @since
 */
@Configuration
@ComponentScan({"org.hswebframework.web.service.file.simple"
        , "org.hswebframework.web.controller.file"})
public class FileAutoConfiguration {
}
