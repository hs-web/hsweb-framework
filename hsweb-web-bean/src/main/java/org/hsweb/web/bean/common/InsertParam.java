package org.hsweb.web.bean.common;

/**
 * Created by zhouhao on 16-4-19.
 */
public class InsertParam<T> extends SqlParam<InsertParam<T>> {
    private T data;

    public InsertParam() {
    }

    public InsertParam(T data) {
        this.data = data;
    }

    public InsertParam<T> value(T data) {
        this.data = data;
        return this;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> InsertParam<T> build(T data) {
        return new InsertParam<>(data);
    }
}
