package org.hswebframework.web.bean;

import lombok.AllArgsConstructor;
import org.hswebframework.ezorm.core.Extendable;

import java.util.Set;

@AllArgsConstructor
class ExtendableToBeanCopier implements Copier {

    private final Copier copier;

    @Override
    public void copy(Object source, Object target, Set<String> ignore, Converter converter) {
        copier.copy(source, target, ignore, converter);
        FastBeanCopier.copy(((Extendable) source).extensions(), target);
    }


}
