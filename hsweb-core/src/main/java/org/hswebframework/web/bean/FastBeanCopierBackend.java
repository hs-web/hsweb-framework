package org.hswebframework.web.bean;

/**
 * Copier backend abstraction for FastBeanCopier.
 *
 * @author zhouhao
 * @since 5.0.2
 */
public interface FastBeanCopierBackend {

    Copier createCopier(Class<?> source, Class<?> target);
}
