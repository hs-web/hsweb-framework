package org.hswebframework.web.bean;

import org.hswebframework.ezorm.core.Extendable;
import org.hswebframework.web.bean.accessor.AsmBeanAccessor;

import java.util.Map;

final class AsmAccessorFastBeanCopierBackend extends AccessorFastBeanCopierBackend {
    private final AsmBeanAccessor asmBeanAccessor;

    AsmAccessorFastBeanCopierBackend() {
        this(new AsmBeanAccessor());
    }

    private AsmAccessorFastBeanCopierBackend(AsmBeanAccessor accessor) {
        super(accessor);
        this.asmBeanAccessor = accessor;
    }

    @Override
    public Copier createCopier(Class<?> source, Class<?> target) {
        if (!Map.class.isAssignableFrom(source)
            && !Map.class.isAssignableFrom(target)
            && !Extendable.class.isAssignableFrom(source)
            && !Extendable.class.isAssignableFrom(target)) {
            Copier copier = asmBeanAccessor.createDirectCopier(source, target);
            if (copier != null) {
                return copier;
            }
        }
        return super.createCopier(source, target);
    }
}
