package org.hswebframework.web.database.manager.meta.table;

import java.io.Serializable;

/**
 * @author zhouhao
 */
public class Constraint implements Serializable {
    private static final long serialVersionUID = 6594361915290310179L;
    private String table;

    private String column;

    private Type type;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        PrimaryKey, ForeignKey, Unique, Check, Default
    }
}
