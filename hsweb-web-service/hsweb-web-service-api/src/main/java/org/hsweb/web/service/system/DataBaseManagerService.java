package org.hsweb.web.service.system;


import org.hsweb.ezorm.rdb.meta.RDBTableMetaData;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 数据库管理服务,用于获取数据库中的表结构等操作
 * Created by zhouhao on 16-4-21.
 */
public interface DataBaseManagerService {

    /**
     * 获取当前数据源中所有的表名
     * 能自动获取数据库类型，并列出对于的表名
     * 当前版本支持数据库:h2,mysql，oracle
     *
     * @return 表名集合
     */
    List<RDBTableMetaData> getTableList() throws SQLException;

    List<Map<String, Object>> execSql(List<String> sqlList) throws SQLException;

    String createAlterSql(RDBTableMetaData newTable) throws Exception;

    String createCreateSql(RDBTableMetaData newTable) throws Exception;
}
