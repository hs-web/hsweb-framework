package org.hswebframework.web.bean;

import lombok.AllArgsConstructor;
import org.hswebframework.ezorm.core.Extensible;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
class MapToExtensibleCopier implements Copier {

    private final Copier copier;

    @Override
    public void copy(Object source, Object target, Set<String> ignore, Converter converter) {
        copier.copy(source, target, ignore, converter);

        ExtensibleUtils.copyFromMap((Map<String, Object>) source, ignore, (Extensible) target);
    }


}
