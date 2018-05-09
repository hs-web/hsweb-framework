package org.hswebframework.web.database.manager.meta.table.parser.support;

import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.database.manager.meta.table.parser.AbstractSqlTableMetaDataParser;
import org.hswebframework.web.datasource.DatabaseType;

public class OracleTableMetaDataParser extends AbstractSqlTableMetaDataParser {

    private final static String TABLE_META_SQL = "select distinct(cols.column_name) as \"name\"" +
            ",cols.table_name as \"tableName\"" +
            ",cols.data_type as \"dataType\"" +
            ",cols.data_length as \"dataLength\"" +
            ",cols.data_precision as \"precision\"" +
            ",cols.data_scale as \"scale\"" +
            ",acc.comments as \"comment\"" +
            ",case when cols.nullable='Y' then 0 else 1 end as \"notNull\"" +
            ",cols.column_id from user_tab_columns cols " +
            "left join all_col_comments acc on acc.column_name=cols.column_name and acc.table_name=cols.table_name " +
            "where cols.table_name=upper(#{table}) " +
            "order by cols.column_id ";

    private final static String TABLE_COMMENT_SQL = "select comments as \"comment\" from user_tab_comments where table_type='TABLE' and table_name=upper(#{table})";

    private final static String ALL_TABLE_SQL = "select table_name as \"name\" from user_tab_comments where table_type='TABLE'";

    public OracleTableMetaDataParser(SqlExecutor sqlExecutor) {
        super(sqlExecutor, DatabaseType.oracle);
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
