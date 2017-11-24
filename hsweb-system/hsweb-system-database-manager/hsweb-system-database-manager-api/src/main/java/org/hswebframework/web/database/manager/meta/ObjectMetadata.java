package org.hswebframework.web.database.manager.meta;

import java.io.Serializable;

/**
 * @author zhouhao
 */
public abstract class ObjectMetadata implements Serializable {

    private static final long serialVersionUID = -5672781818170734240L;

    protected String name;

    protected ObjectType type;

    public ObjectType getType() {
        return type;
    }

    public void setType(ObjectType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public enum ObjectType {
        TABLE,//表
        VIEW,//视图
        SEQUENCES//序列
    }
}
