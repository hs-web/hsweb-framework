package org.hswebframework.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Component
public class ApplicationContextHolder {
    private static ApplicationContext context;

    public static ApplicationContext get() {
        if (null == context) {
            throw new UnsupportedOperationException("ApplicationContext not ready!");
        }
        return context;
    }

    @Autowired
    public void setContext(ApplicationContext context) {
        if (null == ApplicationContextHolder.context) {
            ApplicationContextHolder.context = context;
        }
    }
}
