package org.hswebframework.web.service.form;

import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.record.Record;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;

/**
 * 数据库仓库,用于获取默认数据库信息,指定数据源的信息以及当前激活的数据库信息
 *
 * @author zhouhao
 * @see DatabaseOperator
 * @since 3.0
 */
public interface DatabaseRepository {

    DatabaseOperator getDefaultDatabase(String databaseName);

    DatabaseOperator getDatabase(String datasourceId,String databaseName);

    DatabaseOperator getCurrentDatabase();
}
