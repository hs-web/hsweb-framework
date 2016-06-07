package org.hsweb.web.bean.common;

import com.alibaba.fastjson.JSON;
import org.hsweb.ezorm.param.Term;
import org.hsweb.ezorm.param.TermType;
import org.webbuilder.utils.common.MapUtils;
import org.webbuilder.utils.common.StringUtils;

import java.util.*;

/**
 * Created by zhouhao on 16-4-19.
 */
public class SqlParam<R extends SqlParam> extends org.hsweb.ezorm.param.SqlParam<R> {

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
        return new SqlParam<>();
    }
}
