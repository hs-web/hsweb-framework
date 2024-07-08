package org.hswebframework.web.logging.aop;


import org.hswebframework.web.logging.AccessLoggerListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * AOP 访问日志记录自动配置
 *
 * @author zhouhao
 * @see org.hswebframework.web.logging.AccessLogger
 * @see AopAccessLoggerSupport
 */
@ConditionalOnClass(AccessLoggerListener.class)
@Configuration(proxyBeanMethods = false)
public class AopAccessLoggerSupportAutoConfiguration {

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public AopAccessLoggerSupport aopAccessLoggerSupport() {
        return new AopAccessLoggerSupport();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public ReactiveAopAccessLoggerSupport reactiveAopAccessLoggerSupport() {
        return new ReactiveAopAccessLoggerSupport();
    }

    @Bean
    public DefaultAccessLoggerParser defaultAccessLoggerParser() {
        return new DefaultAccessLoggerParser();
    }

    @Bean
    @ConditionalOnClass(name = "io.swagger.annotations.Api")
    @Order(10)
    public SwaggerAccessLoggerParser swaggerAccessLoggerParser() {
        return new SwaggerAccessLoggerParser();
    }

    @Bean
    @ConditionalOnClass(name = "io.swagger.v3.oas.annotations.tags.Tag")
    @Order(1)
    public Swagger3AccessLoggerParser swagger3AccessLoggerParser() {
        return new Swagger3AccessLoggerParser();
    }

    @Bean
    @ConditionalOnClass(name = "org.hswebframework.web.authorization.annotation.Resource")
    @Order(999)
    public ResourceAccessLoggerParser resourceAccessLoggerParser() {
        return new ResourceAccessLoggerParser();
    }
}
