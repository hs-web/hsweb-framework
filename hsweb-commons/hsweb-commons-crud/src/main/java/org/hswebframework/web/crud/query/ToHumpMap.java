package org.hswebframework.web.crud.query;

import java.util.LinkedHashMap;

public class ToHumpMap<V> extends LinkedHashMap<String, V> {

    @Override
    public V put(String key, V value) {
        V val = super.put(key, value);

        String humpKey = QueryHelperUtils.toHump(key);
        if (!humpKey.equals(key)) {
            super.put(humpKey, value);
        }
        return val;
    }

}
