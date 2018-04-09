package org.hswebframework.web.database.manager;

import org.hswebframework.web.datasource.manager.simple.SimpleDatabaseManagerService;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.hswebframework.web.database.manager.web"
        , "org.hswebframework.web.datasource.manager.simple"})
@ImportAutoConfiguration(TableMetaDataParserAutoConfiguration.class)
@AutoConfigureBefore(TableMetaDataParserAutoConfiguration.class)
public class DataBaseManagerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DatabaseManagerService.class)
    public SimpleDatabaseManagerService simpleDatabaseManagerService() {
        return new SimpleDatabaseManagerService();
    }


}
