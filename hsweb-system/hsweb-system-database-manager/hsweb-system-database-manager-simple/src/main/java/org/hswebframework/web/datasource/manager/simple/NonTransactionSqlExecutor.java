package org.hswebframework.web.datasource.manager.simple;

import org.hswebframework.web.database.manager.SqlExecuteRequest;
import org.hswebframework.web.database.manager.SqlExecuteResult;
import org.hswebframework.web.database.manager.SqlInfo;
import org.hswebframework.web.database.manager.exception.SqlExecuteException;
import org.hswebframework.web.database.manager.sql.SqlExecutor;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class NonTransactionSqlExecutor implements SqlExecutor {
    private org.hswebframework.ezorm.rdb.executor.SqlExecutor executor;


    public NonTransactionSqlExecutor(org.hswebframework.ezorm.rdb.executor.SqlExecutor executor) {
        this.executor = executor;
    }

    @Override
    public List<SqlExecuteResult> execute(SqlExecuteRequest request) throws Exception {
        return request.getSql().stream().map(this::doExecute).collect(Collectors.toList());
    }

    public SqlExecuteResult doExecute(SqlInfo sqlInfo) {
        SqlExecuteResult result = new SqlExecuteResult();
        Object executeResult = null;
        try {
            switch (sqlInfo.getType().toUpperCase()) {
                case "SELECT":
                    QueryResultWrapper wrapper = new QueryResultWrapper();
                    executor.list(sqlInfo.getSql(), wrapper);
                    executeResult = wrapper.getResult();
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
            result.setSuccess(true);
        } catch (SQLException e) {
            throw new SqlExecuteException(e.getMessage(), e, sqlInfo.getSql());
        }
        result.setResult(executeResult);
        result.setSqlInfo(sqlInfo);

        return result;
    }
}
