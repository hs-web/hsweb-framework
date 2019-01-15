/*
 *  Copyright 2019 http://www.hswebframework.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.hswebframework.web.datasource.starter;

import org.hswebframework.web.datasource.DynamicDataSourceAutoConfiguration;
import org.hswebframework.web.datasource.DynamicDataSourceProxy;
import org.hswebframework.web.datasource.DynamicDataSourceService;
import org.hswebframework.web.service.datasource.DataSourceConfigService;
import org.hswebframework.web.service.datasource.simple.InDBDataSourceRepository;
import org.hswebframework.web.service.datasource.simple.InDBDynamicDataSourceService;
import org.hswebframework.web.service.datasource.simple.InDBJtaDynamicDataSourceService;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author zhouhao
 */
@Configuration
@ComponentScan({"org.hswebframework.web.service.datasource.simple"
        , "org.hswebframework.web.controller.datasource"})
@AutoConfigureBefore(DynamicDataSourceAutoConfiguration.class)
public class InDBDynamicDataSourceAutoConfiguration {

    @Bean
    public InDBDataSourceRepository inDBDataSourceRepository(DataSourceConfigService dataSourceConfigService) {
        return new InDBDataSourceRepository(dataSourceConfigService);
    }

    @Bean
    @ConditionalOnMissingClass("org.hswebframework.web.datasource.jta.JtaDynamicDataSourceService")
    public DynamicDataSourceService inDBDynamicDataSourceService(InDBDataSourceRepository repository,
                                                                 DataSource dataSource) {
        return new InDBDynamicDataSourceService(repository, new DynamicDataSourceProxy("dataSource", dataSource));
    }

    @Configuration
    @ConditionalOnClass(org.hswebframework.web.datasource.jta.JtaDynamicDataSourceService.class)
    public static class InDBJtaDynamicDataSourceServiceAutoConfiguration {
        @Bean
        public DynamicDataSourceService inDBJtaDynamicDataSourceService(InDBDataSourceRepository repository,
                                                                        DataSource dataSource) {
            return new InDBJtaDynamicDataSourceService(repository, new DynamicDataSourceProxy("dataSource", dataSource));
        }
    }

}
