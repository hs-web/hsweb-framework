package org.hswebframework.web.database.manager.meta.table.parser.support;

import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.web.database.manager.meta.table.parser.AbstractSqlTableMetaDataParser;
import org.hswebframework.web.datasource.DatabaseType;

public class SqlServerTableMetaDataParser extends AbstractSqlTableMetaDataParser {
    private static String TABLE_META_SQL = "SELECT \n" +
            "c.name as [name],\n" +
            "t.name as [dataType],\n" +
            "c.length as [length],\n" +
            "c.xscale as [scale],\n" +
            "c.xprec as [precision],\n" +
            "case when c.isnullable=1 then 0 else  1 end as [notNull],\n" +
            "cast(p.value as varchar(500)) as [comment]\n" +
            "FROM syscolumns c\n" +
            "inner join  systypes t on c.xusertype = t.xusertype \n" +
            "left join sys.extended_properties p on c.id=p.major_id and c.colid=p.minor_id\n" +
            "WHERE c.id = object_id(#{table})";

    private static String TABLE_COMMENT_SQL = "select cast(p.value as varchar(500)) as [comment] from sys.extended_properties p " +
            " where p.major_id=object_id(#{table}) and p.minor_id=0";

    public SqlServerTableMetaDataParser(SqlExecutor sqlExecutor) {
        super(sqlExecutor, DatabaseType.sqlserver, DatabaseType.jtds_sqlserver);
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
        return "select name from sysobjects where xtype='U'";
    }

}
