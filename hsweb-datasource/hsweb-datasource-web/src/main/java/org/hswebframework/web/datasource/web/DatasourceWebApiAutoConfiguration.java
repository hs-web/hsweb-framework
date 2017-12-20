package org.hswebframework.web.datasource.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasourceWebApiAutoConfiguration {

    @Bean
    public DatasourceController datasourceController() {
        return new DatasourceController();
    }
}
