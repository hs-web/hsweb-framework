package org.hswebframework.web.database.manager.meta.table.parser.support;

import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.database.manager.meta.table.parser.AbstractSqlTableMetaDataParser;
import org.hswebframework.web.datasource.DatabaseType;

public class PostgresTableMetaDataParser extends AbstractSqlTableMetaDataParser {
    private static final String TABLE_META_SQL = "select column_name as \"name\"" +
            " , udt_name as \"dataType\"" +
            " , table_name as \"tableName\"" +
            " , character_maximum_length as \"dataLength\"" +
            " , numeric_precision as \"precision\"" +
            " , numeric_scale as \"scale\"" +
            " , case when is_nullable = 'YES' then 0 else 1 end as \"notNull\"" +
            " ,col_description(a.attrelid,a.attnum) as \"comment\"" +
            " from information_schema.columns columns ," +
            "     pg_class as c,pg_attribute as a" +
            " where a.attrelid = c.oid and a.attnum>0 and a.attname = columns.column_name and c.relname=columns.table_name" +
            " and table_schema = current_schema()" +
            "  and table_name = #{table}";

    private static final String TABLE_COMMENT_SQL = "select cast(obj_description(relfilenode,'pg_class') as varchar)" +
            "  as \"comment\" from pg_class c" +
            " where relname=#{table} and relkind = 'r' and relname not like 'pg_%'" +
            " and relname not like 'sql_%'";

    private static final String ALL_TABLE_SQL = "select table_name as \"name\" from information_schema.TABLES where table_schema=current_schema()";

    public PostgresTableMetaDataParser(SqlExecutor sqlExecutor) {
        super(sqlExecutor, DatabaseType.postgresql);
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
