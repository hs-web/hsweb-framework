package org.hswebframework.web.database.manager;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhouhao
 */
public class SqlExecuteRequest implements Serializable{
    private List<SqlInfo> sql;

    public List<SqlInfo> getSql() {
        return sql;
    }

    public void setSql(List<SqlInfo> sql) {
        this.sql = sql;
    }
}
