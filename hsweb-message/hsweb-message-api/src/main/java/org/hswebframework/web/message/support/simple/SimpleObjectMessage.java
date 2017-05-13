package org.hswebframework.web.message.support.simple;

import org.hswebframework.web.message.support.ObjectMessage;

import java.io.Serializable;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleObjectMessage<T extends Serializable> implements ObjectMessage<T> {

    private T object;

    @Override
    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public SimpleObjectMessage(T object) {
        this.object = object;
    }

    public SimpleObjectMessage() {
    }
}
