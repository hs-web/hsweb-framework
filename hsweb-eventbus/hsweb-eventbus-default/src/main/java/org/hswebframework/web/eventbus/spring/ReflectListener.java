package org.hswebframework.web.eventbus.spring;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.eventbus.EventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author zhouhao
 * @since 3.0
 */
@Slf4j
public class ReflectListener<E> implements EventListener<E> {

    private Object target;
    private Method method;

    public ReflectListener(Object target, Method method) {
        this.target = target;
        this.method = method;
    }

    @Override
    public void onEvent(E event) {
        try {
            method.invoke(target, event);
        } catch (Exception e) {
            log.error("反射执行事件失败. target={},method={},event={}", target, method, event, e);
        }
    }
}
