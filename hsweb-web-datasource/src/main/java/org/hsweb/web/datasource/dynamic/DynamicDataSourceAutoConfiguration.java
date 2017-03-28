/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.datasource.dynamic;

import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hsweb.commons.ClassUtils;
import org.hsweb.commons.StringUtils;
import org.hsweb.web.core.datasource.DataSourceHolder;
import org.hsweb.web.core.datasource.DynamicDataSource;
import org.hsweb.web.core.utils.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jta.atomikos.AtomikosProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import javax.transaction.SystemException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Properties;

@Configuration
@ConditionalOnMissingBean(DynamicDataSource.class)
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
@ComponentScan("org.hsweb.web.datasource.dynamic")
public class DynamicDataSourceAutoConfiguration {

    @Autowired
    private DynamicDataSourceProperties properties;

    @Bean(initMethod = "init", destroyMethod = "shutdownForce")
    public UserTransactionServiceImp userTransactionService() {
        AtomikosProperties atomikosProperties = properties.getIcatch();
        Properties properties = new Properties();
        properties.putAll(atomikosProperties.asProperties());
        if (StringUtils.isNullOrEmpty(properties.get("com.atomikos.icatch.service"))) {
            properties.put("com.atomikos.icatch.service", "com.atomikos.icatch.standalone.UserTransactionServiceFactory");
        }
        return new UserTransactionServiceImp(properties);
    }

    /**
     * 默认数据库链接
     */
    @Primary
    @Bean(initMethod = "init", name = "dataSource", destroyMethod = "close")
    @ConditionalOnMissingBean(DataSource.class)
    @Cacheable
    public DataSource dataSource() {
        AtomikosDataSourceBean dataSourceBean = new AtomikosDataSourceBean();
        properties.putProperties(dataSourceBean);
        return dataSourceBean;
    }

    @Bean(name = "dynamicDataSource")
    public DynamicXaDataSourceImpl dynamicXaDataSource(@Qualifier("dataSource") DataSource dataSource) {
        DynamicXaDataSourceImpl dynamicXaDataSource = new DynamicXaDataSourceImpl(dataSource, properties.getType());
        DataSourceHolder.install(dynamicXaDataSource);
        return dynamicXaDataSource;
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public UserTransactionManager userTransactionManager(
            UserTransactionService userTransactionService) {
        UserTransactionManager transactionManager = new UserTransactionManager();
        transactionManager.setForceShutdown(true);
        transactionManager.setStartupTransactionService(false);
        return transactionManager;
    }

    @Bean
    public UserTransactionImp userTransaction() throws SystemException {
        UserTransactionImp userTransactionImp = new UserTransactionImp();
        userTransactionImp.setTransactionTimeout(properties.getTransactionTimeout());
        return userTransactionImp;
    }

    @Bean
    public JtaTransactionManager transactionManager(UserTransactionManager userTransactionManager, UserTransactionImp userTransaction) throws SystemException {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(userTransactionManager);
        jtaTransactionManager.setUserTransaction(userTransaction);
        jtaTransactionManager.setAllowCustomIsolationLevels(true);
        return jtaTransactionManager;
    }

    @Bean(name = "sqlExecutor")
    @ConditionalOnMissingBean(DynamicDataSourceSqlExecutorService.class)
    public DynamicDataSourceSqlExecutorService sqlExecutor() {
        return new DynamicDataSourceSqlExecutorService();
    }

    @Bean
    public AnnotationDataSourceChangeConfiguration annotationDataSourceChangeConfiguration() {
        return new AnnotationDataSourceChangeConfiguration();
    }

    @Aspect
    public static class AnnotationDataSourceChangeConfiguration {

        private <T extends Annotation> T getAnn(ProceedingJoinPoint pjp, Class<T> annClass) {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method m = signature.getMethod();
            T a = AnnotationUtils.findAnnotation(m, annClass);
            if (a != null) return a;
            Class<?> targetClass = pjp.getTarget().getClass();
            m = org.springframework.util.ClassUtils.getMostSpecificMethod(m, targetClass);
            a = AnnotationUtils.findAnnotation(m, annClass);
            if (a != null) return a;
            return AnnotationUtils.findAnnotation(pjp.getTarget().getClass(), annClass);
        }

        @Around(value = "within(@org.hsweb.web.datasource.dynamic.UseDataSource *)||@annotation(org.hsweb.web.datasource.dynamic.UseDataSource)")
        public Object useDatasource(ProceedingJoinPoint pjp) throws Throwable {
            UseDataSource ann = getAnn(pjp, UseDataSource.class);
            try {
                if (null != ann) {
                    DynamicDataSource.use(ann.value());
                }
                return pjp.proceed();
            } finally {
                if (null != ann) {
                    DynamicDataSource.useDefault(false);
                }
            }
        }

        @Around(value = "within(@org.hsweb.web.datasource.dynamic.UseDefaultDataSource *)||@annotation(org.hsweb.web.datasource.dynamic.UseDataSource)")
        public Object useDefaultDatasource(ProceedingJoinPoint pjp) throws Throwable {
            UseDefaultDataSource ann = getAnn(pjp, UseDefaultDataSource.class);
            try {
                if (null != ann) {
                    DynamicDataSource.useDefault(ann.value());
                }
                return pjp.proceed();
            } finally {
                if (ann != null && ann.value())
                    DynamicDataSource.useLast();
            }
        }
    }
}
