package org.hswebframework.web.datasource.manager.simple;

import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.database.manager.SqlExecuteRequest;
import org.hswebframework.web.database.manager.SqlExecuteResult;
import org.hswebframework.web.database.manager.SqlInfo;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 默认的事务执行器
 *
 * @author zhouhao
 */
public class DefaultLocalTransactionExecutor implements TransactionExecutor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Queue<Execution> executionQueue = new LinkedList<>();

    private SqlExecutor sqlExecutor;

    private SqlRequestExecutor sqlRequestExecutor;

    private String transactionId;

    private String datasourceId;

    private volatile boolean shutdown = false;

    private TransactionStatus transactionStatus;

    private TransactionTemplate transactionTemplate;

    private boolean commit = false;

    private volatile boolean running = false;

    /* 线程循环开始等待sql进入的时候执行一次,sql进入的时候执行一次,然后唤醒线程开始执行sql */
    private CyclicBarrier waitToReady = new CyclicBarrier(2);

    private CountDownLatch waitClose = new CountDownLatch(1);

    public DefaultLocalTransactionExecutor(SqlExecutor sqlExecutor, String transactionId, String datasourceId, TransactionTemplate transactionTemplate) {
        this.sqlExecutor = sqlExecutor;
        this.transactionId = transactionId;
        this.datasourceId = datasourceId;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public String getDatasourceId() {
        return datasourceId;
    }

    @Override
    public void commit() {
        commit = true;
        shutdown = true;
        waitToClose();
    }

    protected void waitToClose() {
        try {
            logger.debug("wait transaction {} close", transactionId);
            if (!running) {
                //先唤醒执行,继续执行任务
                waitToReady.await();
            }
            //等待执行结束
            waitClose.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rollback() {
        shutdown = true;
        waitToClose();
    }

    public void setSqlExecutor(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }

    public void setSqlRequestExecutor(SqlRequestExecutor sqlRequestExecutor) {
        this.sqlRequestExecutor = sqlRequestExecutor;
    }

    protected void buildDefaultSqlRequestExecutor() {
        sqlRequestExecutor = (executor, sqlInfo) -> {
            SqlExecuteResult result = new SqlExecuteResult();
            Object executeResult = null;

            switch (sqlInfo.getType().toUpperCase()) {
                case "SELECT":
                    executeResult = executor.list(sqlInfo.getSql());
                    break;
                case "INSERT":
                case "UPDATE":
                    executeResult = executor.update(sqlInfo.getSql());
                    break;
                case "DELETE":
                    executeResult = executor.delete(sqlInfo.getSql());
                    break;
                default:
                    executor.exec(sqlInfo.getSql());
            }
            result.setResult(executeResult);
            result.setSqlInfo(sqlInfo);
            return result;
        };
    }

    @Override
    public void run() {
        try {
            if (datasourceId != null) {
                DataSourceHolder.switcher().use(datasourceId);
            }
            //开启事务
            transactionStatus = transactionTemplate.getTransactionManager().getTransaction(transactionTemplate);
            if (sqlRequestExecutor == null) {
                buildDefaultSqlRequestExecutor();
            }
            while (!shutdown) {
                logger.debug("wait sql execute request {}", transactionId);
                waitToReady.await();//等待有新的sql进来
                waitToReady.reset();//重置,下一次循环继续等待
                //执行sql
                doExecute();
            }
        } catch (Exception e) {
            rollback();//回滚
            logger.error("execute sql error {}", transactionId, e);
        } finally {
            try {
                if (commit) {
                    logger.debug("Commit transaction {}", transactionId);
                    transactionTemplate.getTransactionManager().commit(transactionStatus);
                } else {
                    logger.debug("Roll Back transaction {}", transactionId);
                    transactionTemplate.getTransactionManager().rollback(transactionStatus);
                }
                //结束事务
                waitClose.countDown();
            } finally {
                DataSourceHolder.switcher().reset();
            }
        }
    }

    protected void doExecute() {
        Execution execution;
        while ((execution = executionQueue.poll()) != null) {
            running = true;
            logger.debug("start execute sql {}", transactionId);
            try {
                List<SqlExecuteResult> requests = execution.request.getSql()
                        .stream()
                        .map(sqlInfo -> {
                            try {
                                //执行sql
                                return sqlRequestExecutor.apply(sqlExecutor, sqlInfo);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toList());
                //通过回调返回执行结果
                execution.callback.accept(requests);
            } catch (Exception e) {
                rollback();
                execution.onError.accept(e);
            }
        }
        running = false;
    }

    @Override
    public List<SqlExecuteResult> execute(SqlExecuteRequest request) {
        if (shutdown) {
            throw new UnsupportedOperationException("transaction is close");
        }
        //执行倒计时,执行sql是异步的,通过此方式等待sql执行完毕
        CountDownLatch countDownLatch = new CountDownLatch(1);
        List<SqlExecuteResult> results = new ArrayList<>();

        //异常信息
        Exception[] exceptions = new Exception[1];

        Execution execution = new Execution();
        execution.request = request;
        execution.callback = sqlExecuteResults -> {
            results.addAll(sqlExecuteResults);
            sqlExecuteResults.clear();
            countDownLatch.countDown();
        };
        execution.onError = e -> {
            exceptions[0] = e;
            countDownLatch.countDown();
        };
        logger.debug("submit sql execute job {}", transactionId);
        executionQueue.add(execution);
        try {
            //当前没有在执行sql,说明现在正在等待新的sql进入,唤醒之
            if (!running)
                waitToReady.await();
            //等待sql执行完毕
            countDownLatch.await();
            //判断是否有异常
            Exception exception;
            if ((exception = exceptions[0]) != null) {
                if (exception instanceof RuntimeException) {
                    throw (RuntimeException) exception;
                } else {
                    throw new RuntimeException(exception);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    protected class Execution {
        protected SqlExecuteRequest request;

        protected Consumer<List<SqlExecuteResult>> callback;

        protected Consumer<Exception> onError;
    }

    private interface SqlRequestExecutor {
        SqlExecuteResult apply(SqlExecutor executor, SqlInfo sqlInfo) throws SQLException;
    }

}
