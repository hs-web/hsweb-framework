package org.hswebframework.web.datasource.manager.simple;

import org.hswebframework.ezorm.rdb.executor.SqlRequests;
import org.hswebframework.web.database.manager.SqlExecuteRequest;
import org.hswebframework.web.database.manager.SqlExecuteResult;
import org.hswebframework.web.database.manager.SqlInfo;
import org.hswebframework.web.database.manager.exception.SqlExecuteException;
import org.hswebframework.web.database.manager.sql.SqlExecutor;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class NonTransactionSqlExecutor implements SqlExecutor {
    private org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor executor;


    public NonTransactionSqlExecutor(org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor executor) {
        this.executor = executor;
    }

    @Override
    public List<SqlExecuteResult> execute(SqlExecuteRequest request) throws Exception {
        return request.getSql().stream().map(this::doExecute).collect(Collectors.toList());
    }

    public SqlExecuteResult doExecute(SqlInfo sqlInfo) {
        SqlExecuteResult result = new SqlExecuteResult();
        Object executeResult = null;
        switch (sqlInfo.getType().toUpperCase()) {
            case "SELECT":
                QueryResultWrapper wrapper = new QueryResultWrapper();
                executor.select(SqlRequests.of(sqlInfo.getSql()), wrapper);
                executeResult = wrapper.getResult();
                break;
            case "INSERT":
            case "DELETE":
            case "UPDATE":
                executeResult = executor.update(SqlRequests.of(sqlInfo.getSql()));
            default:
                executor.execute(SqlRequests.of(sqlInfo.getSql()));
        }
        result.setSuccess(true);

        result.setResult(executeResult);
        result.setSqlInfo(sqlInfo);

        return result;
    }
}
