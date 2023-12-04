package org.hswebframework.web.datasource.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
public class DatasourceWebApiAutoConfiguration {

    @Bean
    public DatasourceController datasourceController() {
        return new DatasourceController();
    }
}
