package org.hsweb.web.service.system;

/**
 * SQL执行过程
 * Created by zhouhao on 16-4-21.
 */
public interface SqlExecuteProcess {
    /**
     * 执行sql前
     *
     * @param sql
     */
    void before(String sql);

    /**
     * 执行sql后
     *
     * @param sql    被执行的sql语句
     * @param result 执行结果
     */
    void after(String sql, Object result);

    /**
     * 执行sql失败
     *
     * @param sql       被执行的sql语句
     * @param throwable 执行失败异常信息
     */
    void error(String sql, Throwable throwable);
}
