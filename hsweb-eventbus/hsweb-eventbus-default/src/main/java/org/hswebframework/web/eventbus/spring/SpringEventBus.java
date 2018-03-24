package org.hswebframework.web.eventbus.spring;

import org.hswebframework.web.eventbus.AbstractEventBus;
import org.hswebframework.web.eventbus.EventListenerDefine;
import org.hswebframework.web.eventbus.annotation.Subscribe;
import org.hswebframework.web.eventbus.executor.EventListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;

/**
 * @author zhouhao
 * @since 3.0
 */
public class SpringEventBus extends AbstractEventBus implements BeanPostProcessor {

    @Override
    protected EventListenerContainer createEventListenerContainer() {
        return new SpringEventContainer();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return processListener(bean);
    }

    protected Object processListener(Object object) {
        Method[] methods = object.getClass().getDeclaredMethods();
        for (Method method : methods) {
            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            if (subscribe != null) {
                createListener(subscribe, object, method);
            }
        }
        return object;
    }

    protected void createListener(Subscribe type, Object target, Method method) {
        EventListenerDefine define = EventListenerDefine.builder().eventMode(type.mode())
                .transaction(type.transaction())
                .listener(new FastListener(target, method))
                .build();

        subscribe(method.getParameterTypes()[0], define);
    }
}
