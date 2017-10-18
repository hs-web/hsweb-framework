package org.hswebframework.web.file.starter;

import org.hswebframework.web.service.file.FileService;
import org.hswebframework.web.service.file.simple.LocalFileService;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 3.0
 */
@Configuration
@ComponentScan({"org.hswebframework.web.service.file.simple"
        , "org.hswebframework.web.controller.file"})
@ImportAutoConfiguration(FastdfsServiceAutoConfiguration.class)
public class FileAutoConfiguration {

    @ConditionalOnMissingBean(FileService.class)
    @Bean
    public LocalFileService localFileService() {
        return new LocalFileService();
    }
}
