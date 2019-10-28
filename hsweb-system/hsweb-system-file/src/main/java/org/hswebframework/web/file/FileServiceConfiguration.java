package org.hswebframework.web.file;

import org.hswebframework.web.file.web.ReactiveFileController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FileUploadProperties.class)
public class FileServiceConfiguration {


    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "reactiveFileController")
        private ReactiveFileController reactiveFileController() {
            return new ReactiveFileController();
        }
    }

}
