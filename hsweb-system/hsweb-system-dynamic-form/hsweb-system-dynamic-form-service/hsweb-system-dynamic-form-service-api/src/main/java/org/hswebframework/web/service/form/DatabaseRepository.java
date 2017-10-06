package org.hswebframework.web.service.form;

import org.hswebframework.ezorm.rdb.RDBDatabase;

/**
 * 数据库仓库,用于获取默认数据库信息,指定数据源的信息以及当前激活的数据库信息
 *
 * @author zhouhao
 * @see RDBDatabase
 * @since 3.0
 */
public interface DatabaseRepository {
    RDBDatabase getDefaultDatabase();

    RDBDatabase getDatabase(String datasourceId);

    RDBDatabase getCurrentDatabase();
}
