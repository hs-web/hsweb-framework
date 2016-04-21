package org.hsweb.web.bean.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouhao on 16-4-21.
 */
public class InsertMapParam extends InsertParam<Map<String, Object>> {

    public InsertMapParam() {
        this(new HashMap<>());
    }

    public InsertMapParam(Map<String, Object> data) {
        setData(data);
    }

    public InsertMapParam value(String key, Object value) {
        getData().put(key, value);
        return this;
    }
}
