package org.hswebframework.web.datasource.manager.simple;

import org.hswebframework.web.database.manager.SqlExecuteRequest;
import org.hswebframework.web.database.manager.SqlExecuteResult;

import java.util.List;

/**
 * @author zhouhao
 */
public interface TransactionExecutor extends Runnable {
    String getTransactionId();

    String getDatasourceId();

    void commit();

    void rollback();

    List<SqlExecuteResult> execute(SqlExecuteRequest request)throws Exception;

}
