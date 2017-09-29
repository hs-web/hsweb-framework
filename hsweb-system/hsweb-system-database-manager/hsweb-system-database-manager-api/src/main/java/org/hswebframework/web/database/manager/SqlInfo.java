package org.hswebframework.web.database.manager;

import java.io.Serializable;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SqlInfo implements Serializable {
    private String sql;

    private String type;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
