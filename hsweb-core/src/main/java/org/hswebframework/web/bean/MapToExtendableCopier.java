package org.hswebframework.web.bean;

import lombok.AllArgsConstructor;
import org.hswebframework.ezorm.core.Extendable;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
class MapToExtendableCopier implements Copier {

    private final Copier copier;

    @Override
    public void copy(Object source, Object target, Set<String> ignore, Converter converter) {
        copier.copy(source, target, ignore, converter);

        ExtendableUtils.copyFromMap((Map<String, Object>) source, ignore, (Extendable) target);
    }


}
