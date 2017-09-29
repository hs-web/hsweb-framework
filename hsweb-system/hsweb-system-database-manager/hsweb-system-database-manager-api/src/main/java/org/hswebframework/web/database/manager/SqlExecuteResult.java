package org.hswebframework.web.database.manager;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SqlExecuteResult {

    private SqlInfo sqlInfo;

    private Object result;

    public SqlInfo getSqlInfo() {
        return sqlInfo;
    }

    public void setSqlInfo(SqlInfo sqlInfo) {
        this.sqlInfo = sqlInfo;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
