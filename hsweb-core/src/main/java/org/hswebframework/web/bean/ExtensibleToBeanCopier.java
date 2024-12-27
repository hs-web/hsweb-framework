package org.hswebframework.web.bean;

import lombok.AllArgsConstructor;
import org.hswebframework.ezorm.core.Extensible;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
class ExtensibleToBeanCopier implements Copier {

    private final Copier copier;

    @Override
    public void copy(Object source, Object target, Set<String> ignore, Converter converter) {
        copier.copy(source, target, ignore, converter);
        FastBeanCopier.copy(((Extensible) source).extensions(), target);
    }


}
