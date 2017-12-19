package org.hswebframework.web.database.manager.meta.table.parser.support;

import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.database.manager.meta.table.parser.AbstractSqlTableMetaDataParser;
import org.hswebframework.web.datasource.DatabaseType;

public class H2TableMetaDataParser extends AbstractSqlTableMetaDataParser {
   private static final String TABLE_META_SQL = "SELECT " +
            "column_name AS \"name\"" +
            ",type_name AS \"dataType\"" +
            ",character_maximum_length as \"length\"" +
            ",numeric_precision as \"precision\"" +
            ",numeric_scale as \"scale\"" +
            ",case when is_nullable='YES' then 0 else 1 end as \"notNull\"" +
            ",remarks as \"comment\" " +
            "FROM information_schema.columns WHERE TABLE_NAME = upper(#{table})";

    private  static final String TABLE_COMMENT_SQL = "SELECT " +
            "remarks as \"comment\" " +
            "FROM information_schema.tables " +
            "WHERE table_type='TABLE' and table_name=upper(#{table})";

    private static final String ALL_TABLE_SQL = "select table_name as \"name\" FROM information_schema.tables where table_type='TABLE'";

    public H2TableMetaDataParser(SqlExecutor sqlExecutor) {
        super(sqlExecutor,DatabaseType.h2);
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
