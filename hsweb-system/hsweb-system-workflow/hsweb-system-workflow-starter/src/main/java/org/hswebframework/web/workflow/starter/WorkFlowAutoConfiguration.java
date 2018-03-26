package org.hswebframework.web.workflow.starter;

import org.hswebframework.web.service.workflow.ActDefService;
import org.hswebframework.web.service.workflow.simple.SimpleActDefService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.hswebframework.web.workflow")
public class WorkFlowAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ActDefService.class)
    public SimpleActDefService simpleActDefService(){
        return new SimpleActDefService();
    }
}
