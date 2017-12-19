package org.hswebframework.web.database.manager;

import org.hswebframework.web.database.manager.meta.ObjectMetadata;
import org.hswebframework.web.database.manager.sql.SqlExecutor;
import org.hswebframework.web.database.manager.sql.TransactionSqlExecutor;
import org.hswebframework.web.datasource.DynamicDataSource;

import java.util.List;
import java.util.Map;

/**
 * @author zhouhao
 */
public interface DatabaseManagerService extends SqlExecutor,TransactionSqlExecutor {
    Map<ObjectMetadata.ObjectType, List<? extends ObjectMetadata>> getMetas();
}
