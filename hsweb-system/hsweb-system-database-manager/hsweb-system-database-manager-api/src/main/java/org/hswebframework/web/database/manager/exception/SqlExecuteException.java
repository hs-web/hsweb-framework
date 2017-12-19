package org.hswebframework.web.database.manager.exception;

public class SqlExecuteException extends RuntimeException {
    private String sql;

    public SqlExecuteException(String message, Throwable cause, String sql) {
        super(message, cause);
        this.sql = sql;
    }
}
