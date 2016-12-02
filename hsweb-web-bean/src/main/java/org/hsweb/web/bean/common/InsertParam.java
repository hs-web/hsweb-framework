package org.hsweb.web.bean.common;

/**
 * Created by zhouhao on 16-4-19.
 */
public class InsertParam<T> extends org.hsweb.ezorm.core.param.InsertParam<T> {

    public InsertParam() {
    }

    public InsertParam(T data) {
        setData(data);
    }

    public static <T> InsertParam<T> build(T data) {
        return new InsertParam<>(data);
    }
}
