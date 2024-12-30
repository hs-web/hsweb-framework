package org.hswebframework.web.bean;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.ezorm.core.Extendable;

import java.util.Map;
import java.util.Set;

public class ExtendableUtils {

    public static void copyFromMap(Map<String, Object> source,
                                   Set<String> ignore,
                                   Extendable target) {
        ClassDescription def = ClassDescriptions.getDescription(target.getClass());

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            //只copy没有定义的数据
            if (!ignore.contains(entry.getKey()) && !def.getFields().containsKey(entry.getKey())) {
                target.setExtension(entry.getKey(), entry.getValue());
            }
        }

    }

    public static void copyToMap(Extendable target,
                                 Set<String> ignore,
                                 Map<String, Object> source) {
        if (CollectionUtils.isNotEmpty(ignore)) {
            source.putAll(
                Maps.filterKeys(target.extensions(), key -> !ignore.contains(key))
            );
        } else {
            source.putAll(
                target.extensions()
            );
        }

    }

}
