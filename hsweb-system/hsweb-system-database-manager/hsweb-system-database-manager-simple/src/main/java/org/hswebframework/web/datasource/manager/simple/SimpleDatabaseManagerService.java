package org.hswebframework.web.datasource.manager.simple;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.database.manager.DatabaseManagerService;
import org.hswebframework.web.database.manager.SqlExecuteRequest;
import org.hswebframework.web.database.manager.SqlExecuteResult;
import org.hswebframework.web.database.manager.meta.ObjectMetadata;
import org.hswebframework.web.database.manager.meta.table.parser.MetaDataParser;
import org.hswebframework.web.database.manager.meta.table.parser.MetaDataParserRegister;
import org.hswebframework.web.database.manager.sql.TransactionInfo;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.datasource.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
@Slf4j
public class SimpleDatabaseManagerService implements DatabaseManagerService, MetaDataParserRegister {

    private Map<String, TransactionExecutor> transactionExecutorMap = new ConcurrentHashMap<>();

    private Map<String, TransactionInfo> transactionInfoMap = new ConcurrentHashMap<>();

    private ExecutorService executorService;

    private SqlExecutor sqlExecutor;

    private TransactionTemplate transactionTemplate;

    private Map<DatabaseType, Map<ObjectMetadata.ObjectType, MetaDataParser<? extends ObjectMetadata>>> parserRepo = new HashMap<>();

    @Override
    public List<TransactionInfo> allTransaction() {
        return new ArrayList<>(transactionInfoMap.values());
    }

    @PostConstruct
    public void init() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
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
        TransactionInfo info = new TransactionInfo();
        info.setId(id);
        info.setCreateTime(new Date());
        transactionInfoMap.put(id, info);
        return id;
    }

    @Override
    public String newTransaction() {
        return newTransaction(null);
    }

    @Override
    public void commit(String transactionId) {
        try {
            TransactionExecutor executor = transactionExecutorMap.get(transactionId);
            if (executor != null) {
                executor.commit();
            }
        } finally {
            transactionExecutorMap.remove(transactionId);
            transactionInfoMap.remove(transactionId);
        }
    }

    @Override
    public void rollback(String transactionId) {
        try {
            TransactionExecutor executor = transactionExecutorMap.get(transactionId);
            if (executor != null) {
                executor.rollback();
            }
        } finally {
            transactionExecutorMap.remove(transactionId);
            transactionInfoMap.remove(transactionId);
        }
    }

    @Override
    public List<SqlExecuteResult> execute(String transactionId, SqlExecuteRequest request) throws Exception {
        TransactionExecutor executor = transactionExecutorMap.get(transactionId);
        if (executor != null) {
            TransactionInfo info = transactionInfoMap.get(transactionId);
            if (null != info) {
                info.setLastExecuteTime(new Date());
                info.getSqlHistory().addAll(request.getSql());
            }
            return executor.execute(request);
        }
        return null;
    }

    @Override
    public List<SqlExecuteResult> execute(SqlExecuteRequest request) throws Exception {
        return new NonTransactionSqlExecutor(sqlExecutor).execute(request);
    }

    @Override
    public Map<ObjectMetadata.ObjectType, List<? extends ObjectMetadata>> getMetas() {
        return parserRepo
                .computeIfAbsent(DataSourceHolder.currentDatabaseType(), t -> new HashMap<>())
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    try {
                        return entry.getValue().parseAll();
                    } catch (SQLException e) {
                        log.error("parse meta {} error", entry.getKey(), e);
                        return new ArrayList<>();
                    }
                }));
    }

    @Override
    public <M extends ObjectMetadata> void registerMetaDataParser(DatabaseType databaseType, ObjectMetadata.ObjectType objectType, MetaDataParser<M> parser) {
        parserRepo.computeIfAbsent(databaseType, t -> new HashMap<>())
                .put(objectType, parser);
    }
}
