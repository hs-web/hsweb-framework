package org.hswebframework.web.bean;

import org.hswebframework.web.bean.accessor.ReflectionBeanAccessor;

final class ReflectionAccessorFastBeanCopierBackend extends AccessorFastBeanCopierBackend {

    ReflectionAccessorFastBeanCopierBackend() {
        super(new ReflectionBeanAccessor());
    }
}
