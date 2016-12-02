package org.hsweb.web.bean.common;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouhao on 16-4-19.
 */
public class SqlParam extends org.hsweb.ezorm.core.param.Param {

    protected Map<String, Object> params = new HashMap<>();

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static SqlParam build() {
        return new SqlParam();
    }
}
