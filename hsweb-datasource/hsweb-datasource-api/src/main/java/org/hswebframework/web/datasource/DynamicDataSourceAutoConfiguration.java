package org.hswebframework.web.datasource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 */
@Configuration
@ImportAutoConfiguration(AopDataSourceSwitcherAutoConfiguration.class)
public class DynamicDataSourceAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public HswebDataSourceProperties hswebDataSouceProperties() {
        return new HswebDataSourceProperties();
    }

    @Bean
    public BeanPostProcessor switcherInitProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DynamicDataSourceService) {
                    DataSourceHolder.dynamicDataSourceService = ((DynamicDataSourceService) bean);
                }

                return bean;
            }
        };
    }


}
