package org.hsweb.web.bean.common;

/**
 * Created by zhouhao on 16-4-19.
 */
public class UpdateParam<T> extends SqlParam<UpdateParam> {
    private T data;

    public UpdateParam() {
    }

    public UpdateParam(T data) {
        this.data = data;
    }

    public UpdateParam<T> set(T data) {
        this.data = data;
        return this;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
