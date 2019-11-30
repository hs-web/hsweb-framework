package org.hswebframework.web.loggin.aop;


import org.hswebframework.web.logging.AccessLoggerListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AOP 访问日志记录自动配置
 *
 * @author zhouhao
 * @see org.hswebframework.web.logging.AccessLogger
 * @see AopAccessLoggerSupport
 */
@ConditionalOnClass(AccessLoggerListener.class)
@Configuration
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
    public DefaultAccessLoggerParser defaultAccessLoggerParser(){
        return new DefaultAccessLoggerParser();
    }

    @Bean
    @ConditionalOnClass(name = "io.swagger.annotations.Api")
    public SwaggerAccessLoggerParser swaggerAccessLoggerParser(){
        return new SwaggerAccessLoggerParser();
    }


    @Bean
    @ConditionalOnClass(name = "org.hswebframework.web.authorization.annotation.Resource")
    public ResourceAccessLoggerParser resourceAccessLoggerParser(){
        return new ResourceAccessLoggerParser();
    }
}
