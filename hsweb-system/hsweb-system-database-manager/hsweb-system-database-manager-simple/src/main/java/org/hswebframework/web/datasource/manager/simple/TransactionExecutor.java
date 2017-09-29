package org.hswebframework.web.datasource.manager.simple;

import org.hswebframework.web.database.manager.SqlExecuteRequest;
import org.hswebframework.web.database.manager.SqlExecuteResult;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface TransactionExecutor extends Runnable {
    String getTransactionId();

    String getDatasourceId();

    void commit();

    void rollback();

    List<SqlExecuteResult> execute(SqlExecuteRequest request);

}
