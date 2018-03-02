package org.hswebframework.web.database.manager.sql;

import org.hswebframework.web.database.manager.SqlExecuteRequest;
import org.hswebframework.web.database.manager.SqlExecuteResult;
import org.hswebframework.web.datasource.DynamicDataSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public interface TransactionSqlExecutor {
    /**
     * 开启一个指定默认数据源的事务,并返回事务ID,在其他操作的时候,使用事务ID共享同一个事务
     *
     * @param datasourceId 数据源ID {@link DynamicDataSource#getId()}
     * @return 事务ID
     */
    String newTransaction(String datasourceId);

    /**
     * 对默认数据源开启事务,并返回事务ID,在其他操作的时候,使用事务ID共享同一个事务
     *
     * @return 事务ID
     */
    String newTransaction();

    /**
     * 提交事务
     *
     * @param transactionId 事务ID
     */
    void commit(String transactionId);

    /**
     * 回滚事务
     *
     * @param transactionId 事务ID
     */
    void rollback(String transactionId);

    /**
     * @return 获取全部事务
     */
    List<TransactionInfo> allTransaction();

    /**
     * 执行
     *
     * @param transactionId
     * @param request
     * @return
     * @throws Exception
     */
    List<SqlExecuteResult> execute(String transactionId, SqlExecuteRequest request) throws Exception;

}
