package org.hswebframework.web.cache;

import org.hswebframework.web.cache.spring.fix.FixUseSupperClassAnnotationParser;
import org.hswebframework.web.cache.spring.fix.FixUseSupperClassCacheOperationSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.interceptor.CacheOperationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 *
 * @author zhouhao
 */
@Configuration
public class FixUseSupperClassAutoConfiguration {
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CacheOperationSource cacheOperationSource() {
        return new FixUseSupperClassCacheOperationSource(new FixUseSupperClassAnnotationParser());
    }
}
