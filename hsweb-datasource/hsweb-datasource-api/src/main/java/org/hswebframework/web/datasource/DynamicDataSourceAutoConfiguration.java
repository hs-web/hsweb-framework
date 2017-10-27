package org.hswebframework.web.datasource;

import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.datasource.service.InMemoryDynamicDataSourceService;
import org.hswebframework.web.datasource.starter.AopDataSourceSwitcherAutoConfiguration;
import org.hswebframework.web.datasource.switcher.DataSourceSwitcher;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author zhouhao
 */
@Configuration
@ImportAutoConfiguration(AopDataSourceSwitcherAutoConfiguration.class)
public class DynamicDataSourceAutoConfiguration implements BeanPostProcessor {

    @Bean
    @ConditionalOnMissingBean(SqlExecutor.class)
    public SqlExecutor sqlExecutor() {
        return new DefaultJdbcExecutor();
    }

    @Bean
    @ConditionalOnMissingBean(DynamicDataSourceService.class)
    public InMemoryDynamicDataSourceService inMemoryDynamicDataSourceService(DataSource dataSource) {
        DynamicDataSourceProxy dataSourceProxy = new DynamicDataSourceProxy(null, dataSource);
        return new InMemoryDynamicDataSourceService(dataSourceProxy);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @ConditionalOnBean(InMemoryDynamicDataSourceService.class)
    @Configuration
    public static class AutoRegisterDataSource implements BeanPostProcessor {

        private InMemoryDynamicDataSourceService dataSourceService;

        @Autowired
        public void setDataSourceService(InMemoryDynamicDataSourceService dataSourceService) {
            DataSourceHolder.dynamicDataSourceService = dataSourceService;
            this.dataSourceService = dataSourceService;
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof DataSource) {
                dataSourceService.registerDataSource(beanName, ((DataSource) bean));
            }
            return bean;
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DynamicDataSourceService) {
            DataSourceHolder.dynamicDataSourceService = ((DynamicDataSourceService) bean);
        }
        if (bean instanceof DataSourceSwitcher) {
            DataSourceHolder.dataSourceSwitcher = ((DataSourceSwitcher) bean);
        }
        return bean;
    }
}
