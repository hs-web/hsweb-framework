package org.hswebframework.web.service;

import org.hswebframework.web.dao.CrudDao;
import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author zhouhao
 * @since 3.0
 */
@SpringBootApplication
@EnableCaching
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SpringTestApplication {

    @Bean
    public EnableCacheTestService enableCacheTestService() {
        return new EnableCacheTestService();
    }

    @Bean
    public EnableCacheAllEvictTestService enableCacheAllEvictTestService() {
        return new EnableCacheAllEvictTestService();
    }

    @Bean
    public EnableCacheTreeTestService enableCacheTreeTestService() {
        return new EnableCacheTreeTestService();
    }

    @Bean
    public EnableCacheAllEvictTreeTestService enableCacheAllEvictTreeTestService() {
        return new EnableCacheAllEvictTreeTestService();
    }
}
