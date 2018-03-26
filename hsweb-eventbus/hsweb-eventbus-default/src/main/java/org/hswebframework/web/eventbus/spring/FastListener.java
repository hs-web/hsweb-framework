package org.hswebframework.web.eventbus.spring;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.boost.Compiler;
import org.hswebframework.web.eventbus.EventListener;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

/**
 * @author zhouhao
 * @since 3.0
 */
@Slf4j
public class FastListener implements EventListener {

    private Object target;

    private FastListenerCallable callable;

    public FastListener(Object target, Method method) {
        Class[] type = method.getParameterTypes();
        Class targetType = ClassUtils.getUserClass(target);
        this.target = target;
        StringBuilder methodImpl = new StringBuilder();

        methodImpl.append("public void call(Object target, Object event) throws Exception{\n")
                .append(targetType.getName())
                .append(" targetObj=(")
                .append(targetType.getName())
                .append(")target;\n").append("targetObj.")
                .append(method.getName())
                .append("((").append(type[0].getName()).append(")event);\n")
                .append("}");

        callable = Compiler.create(FastListenerCallable.class)
                .addMethod(methodImpl.toString())
                .newInstance();
    }

    @Override
    public void onEvent(Object event) {
        try {
            callable.call(target, event);
        } catch (Exception e) {
            log.error("执行event[{}]失败", event, e);
        }
    }

    public interface FastListenerCallable {
        void call(Object target, Object event) throws Exception;
    }
}
