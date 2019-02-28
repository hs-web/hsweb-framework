package org.hswebframework.web.bean;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Diff {

    private String property;

    private Object before;

    private Object after;

    public static List<Diff> of(Object before, Object after) {
        List<Diff> diffs = new ArrayList<>();

        Map<String, Object> beforeMap = FastBeanCopier.copy(before, HashMap::new);
        Map<String, Object> afterMap = FastBeanCopier.copy(after, HashMap::new);

        for (Map.Entry<String, Object> entry : afterMap.entrySet()) {
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
