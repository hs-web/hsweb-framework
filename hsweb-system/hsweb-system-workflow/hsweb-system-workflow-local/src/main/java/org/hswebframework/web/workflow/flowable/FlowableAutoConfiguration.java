package org.hswebframework.web.workflow.flowable;

import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.hswebframework.web.dao.Dao;
import org.hswebframework.web.service.authorization.UserService;
import org.hswebframework.web.workflow.flowable.utils.CustomGroupEntityManager;
import org.hswebframework.web.workflow.flowable.utils.CustomGroupEntityManagerFactory;
import org.hswebframework.web.workflow.flowable.utils.CustomUserEntityManager;
import org.hswebframework.web.workflow.flowable.utils.CustomUserEntityManagerFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@AutoConfigureAfter(FlowableAutoConfiguration.CustomEntityManagerAutoConfiguration.class)
@MapperScan(value = "org.hswebframework.web.workflow.dao", markerInterface = Dao.class
        , sqlSessionFactoryRef = "sqlSessionFactory")
public class FlowableAutoConfiguration {

    @Autowired(required = false)
    private List<SessionFactory> sessionFactories;

    @Autowired(required = false)
    private List<ActivitiEventListener> activitiEventListeners;

    @Bean
    public ProcessEngineConfigurationConfigurer processEngineConfigurationConfigurer() {
        return configuration -> {
            configuration
                    .setAsyncExecutorActivate(false)
//                    .setDatabaseSchemaUpdate("false")
                    .setJobExecutorActivate(false)
                    .setActivityFontName("宋体")
                    .setLabelFontName("宋体")
                    .setAnnotationFontName("宋体");

            if (activitiEventListeners != null) {
                configuration.setEventListeners(activitiEventListeners);
            }
            if (sessionFactories != null) {
                configuration.setCustomSessionFactories(sessionFactories);
            }
        };
    }

    @ConditionalOnBean(UserService.class)
    @Configuration
    public static class CustomEntityManagerAutoConfiguration {

        @Autowired
        private UserService userService;

        @Bean
        public CustomGroupEntityManagerFactory customGroupEntityManagerFactory() {
            return new CustomGroupEntityManagerFactory(new CustomGroupEntityManager(userService));
        }

        @Bean
        public CustomUserEntityManagerFactory customUserEntityManagerFactory() {
            return new CustomUserEntityManagerFactory(new CustomUserEntityManager(userService));
        }

    }
}
