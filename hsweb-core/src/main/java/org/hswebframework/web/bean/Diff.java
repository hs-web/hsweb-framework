package org.hswebframework.web.bean;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Diff {

    private String property;

    private Object before;

    private Object after;


    public static List<Diff> of(Object before, Object after, String... ignoreProperty) {
        List<Diff> diffs = new ArrayList<>();
        Set<String> ignores = Sets.newHashSet(ignoreProperty);

        Map<String, Object> beforeMap = FastBeanCopier.copy(before, HashMap::new);
        Map<String, Object> afterMap = FastBeanCopier.copy(after, HashMap::new);

        for (Map.Entry<String, Object> entry : afterMap.entrySet()) {
            if (ignores.contains(entry.getKey())) {
                continue;
            }
            Object afterValue = entry.getValue();
            String key = entry.getKey();
            Object beforeValue = beforeMap.get(key);
            if (!CompareUtils.compare(beforeValue, afterValue)) {
                diffs.add(new Diff(key, beforeValue, afterValue));
            }
        }
        return diffs;

    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
