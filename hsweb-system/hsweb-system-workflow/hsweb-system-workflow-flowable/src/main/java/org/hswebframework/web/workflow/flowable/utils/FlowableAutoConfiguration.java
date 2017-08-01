package org.hswebframework.web.workflow.flowable.utils;

import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Administrator on 2017/7/26.
 */
@Configuration
public class FlowableAutoConfiguration {

    @Bean
    public ProcessEngineConfiguration processEngineConfiguration(){
        ProcessEngineConfiguration processEngineConfiguration = new StandaloneProcessEngineConfiguration()
                .setAsyncExecutorActivate(false)
                .setActivityFontName("宋体")
                .setLabelFontName("宋体")
                .setAnnotationFontName("宋体");

        return processEngineConfiguration;
    }

}
