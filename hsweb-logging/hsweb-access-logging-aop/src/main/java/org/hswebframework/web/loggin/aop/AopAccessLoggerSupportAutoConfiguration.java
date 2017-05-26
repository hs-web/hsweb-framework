package org.hswebframework.web.loggin.aop;


import org.hswebframework.web.logging.AccessLoggerListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
public class AopAccessLoggerSupportAutoConfiguration {

    @Bean
    public AopAccessLoggerSupport aopAccessLoggerSupport() {
        return new AopAccessLoggerSupport();
    }

    @Bean
    public ListenerProcessor listenerProcessor() {
        return new ListenerProcessor();
    }

    public static class ListenerProcessor implements BeanPostProcessor {

        @Autowired
        private AopAccessLoggerSupport aopAccessLoggerSupport;

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof AccessLoggerListener) {
                aopAccessLoggerSupport.addListener(((AccessLoggerListener) bean));
            }
            return bean;
        }
    }
}
