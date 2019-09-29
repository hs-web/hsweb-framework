package org.hswebframework.web.workflow.starter;

import com.alibaba.fastjson.parser.ParserConfig;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.activiti.spring.boot.ActivitiProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@ComponentScan("org.hswebframework.web.workflow")
public class WorkFlowAutoConfiguration implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        ParserConfig.getGlobalInstance()
                .addAccept("org.hswebframework.web.workflow.dao.entity");
    }

    @Configuration
    @EnableConfigurationProperties(ActivitiProperties.class)
    public static class DataSourceProcessEngineConfiguration extends AbstractProcessEngineAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public SpringProcessEngineConfiguration springProcessEngineConfiguration(
                DataSource dataSource,
                PlatformTransactionManager transactionManager,
                SpringAsyncExecutor springAsyncExecutor) throws IOException {

            return this.baseSpringProcessEngineConfiguration(dataSource, transactionManager, springAsyncExecutor);
        }
    }
}
