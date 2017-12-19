package org.hswebframework.web.database.manager.meta.table.parser.support;

import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.database.manager.meta.table.parser.AbstractSqlTableMetaDataParser;
import org.hswebframework.web.datasource.DatabaseType;

public class MysqlTableMetaDataParser extends AbstractSqlTableMetaDataParser {

    private static final String TABLE_META_SQL = " select " +
            "column_name as `name`, " +
            "data_type as `dataType`, " +
            "character_maximum_length as `length`, " +
            "numeric_precision as `precision`, " +
            "numeric_scale as `scale`, " +
            "column_comment as `comment`, " +
            "case when is_nullable='YES' then 0 else 1 end as 'notNull' " +
            "from information_schema.columns where table_schema=database() and table_name=#{table}";

    private  static final String TABLE_COMMENT_SQL = " select " +
            "table_comment as `comment` " +
            "from information_schema.tables where table_name=#{table}";

    private static final String ALL_TABLE_SQL = "select table_name as `name` from information_schema.`TABLES` where table_schema=database()";

    public MysqlTableMetaDataParser(SqlExecutor sqlExecutor) {
        super(sqlExecutor,DatabaseType.mysql);
    }

    @Override
    public String getSelectTableColumnsSql() {
        return TABLE_META_SQL;
    }

    @Override
    public String getSelectTableMetaSql() {
        return TABLE_COMMENT_SQL;
    }

    @Override
    public String getSelectAllTableSql() {
        return ALL_TABLE_SQL;
    }

}
