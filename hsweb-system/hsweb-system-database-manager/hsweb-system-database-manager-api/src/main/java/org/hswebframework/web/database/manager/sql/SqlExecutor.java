package org.hswebframework.web.database.manager.sql;

import org.hswebframework.web.database.manager.SqlExecuteRequest;
import org.hswebframework.web.database.manager.SqlExecuteResult;

import java.util.List;

public interface SqlExecutor {
    List<SqlExecuteResult> execute(SqlExecuteRequest request)throws Exception;
}
