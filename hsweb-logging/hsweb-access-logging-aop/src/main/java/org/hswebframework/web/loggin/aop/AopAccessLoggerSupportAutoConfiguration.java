package org.hswebframework.web.loggin.aop;


import org.hswebframework.web.logging.AccessLoggerListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
    public AopAccessLoggerSupport aopAccessLoggerSupport() {
        return new AopAccessLoggerSupport();
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

//    @Bean
//    public ListenerProcessor listenerProcessor() {
//        return new ListenerProcessor();
//    }
//
//    public static class ListenerProcessor implements BeanPostProcessor {
//
//        @Autowired
//        private AopAccessLoggerSupport aopAccessLoggerSupport;
//
//        @Override
//        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//            return bean;
//        }
//
//        @Override
//        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//            if (bean instanceof AccessLoggerListener) {
//                aopAccessLoggerSupport.addListener(((AccessLoggerListener) bean));
//            }  if (bean instanceof AccessLoggerParser) {
//                aopAccessLoggerSupport.addParser(((AccessLoggerParser) bean));
//            }
//            return bean;
//        }
//    }
}
