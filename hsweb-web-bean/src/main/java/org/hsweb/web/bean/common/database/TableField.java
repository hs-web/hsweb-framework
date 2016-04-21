package org.hsweb.web.bean.common.database;

/**
 * 数据库表字段
 * Created by zhouhao on 16-4-21.
 */
public class TableField {
    //原型
    private Object prototype;
    private String title;
    private String name;

    public String getTitle() {
        if (title == null)
            title = name;
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getPrototype() {
        return prototype;
    }

    public void setPrototype(Object prototype) {
        this.prototype = prototype;
    }
}
