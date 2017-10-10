package org.hswebframework.web.datasource.manager.simple;

import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.database.manager.DatabaseManagerService;
import org.hswebframework.web.database.manager.SqlExecuteRequest;
import org.hswebframework.web.database.manager.SqlExecuteResult;
import org.hswebframework.web.database.manager.meta.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Service
public class SimpleDatabaseManagerService implements DatabaseManagerService {

    private Map<String, TransactionExecutor> transactionExecutorMap = new ConcurrentHashMap<>();

    private ExecutorService executorService;

    private SqlExecutor sqlExecutor;

    private TransactionTemplate transactionTemplate;

    @PostConstruct
    public void init() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(20);
        }
    }

    @Autowired(required = false)
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Autowired
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Autowired
    public void setSqlExecutor(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }

    @Override
    public String newTransaction(String datasourceId) {
        String id = UUID.randomUUID().toString();
        DefaultLocalTransactionExecutor executor = new DefaultLocalTransactionExecutor(sqlExecutor, id, datasourceId, transactionTemplate);
        transactionExecutorMap.put(id, executor);
        executorService.submit(executor);
        return id;
    }

    @Override
    public String newTransaction() {
        return newTransaction(null);
    }

    @Override
    public void commit(String transactionId) {
        TransactionExecutor executor = transactionExecutorMap.get(transactionId);
        if (executor != null)
            executor.commit();
        transactionExecutorMap.remove(transactionId);
    }

    @Override
    public void rollback(String transactionId) {
        TransactionExecutor executor = transactionExecutorMap.get(transactionId);
        if (executor != null)
            executor.rollback();
        transactionExecutorMap.remove(transactionId);
    }

    @Override
    public List<SqlExecuteResult> execute(String transactionId, SqlExecuteRequest request) {
        TransactionExecutor executor = transactionExecutorMap.get(transactionId);
        if (executor != null)
            return executor.execute(request);
        return null;
    }

    @Override
    public Map<ObjectMetadata.ObjectType, List<ObjectMetadata>> getMetas(String datasourceId) {

        return null;
    }
}
