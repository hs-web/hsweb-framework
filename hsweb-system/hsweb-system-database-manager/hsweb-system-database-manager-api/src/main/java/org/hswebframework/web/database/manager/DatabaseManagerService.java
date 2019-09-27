package org.hswebframework.web.database.manager;

import org.hswebframework.web.database.manager.sql.SqlExecutor;
import org.hswebframework.web.database.manager.sql.TransactionSqlExecutor;

/**
 * @author zhouhao
 */
public interface DatabaseManagerService extends SqlExecutor,TransactionSqlExecutor {
}
