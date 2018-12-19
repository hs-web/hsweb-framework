package org.hswebframework.web.starter.init;

import lombok.AllArgsConstructor;
import org.hswebframework.ezorm.rdb.RDBDatabase;
import org.hswebframework.ezorm.rdb.RDBTable;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.ezorm.rdb.meta.RDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.meta.RDBTableMetaData;
import org.hswebframework.ezorm.rdb.meta.builder.TableBuilder;
import org.hswebframework.ezorm.rdb.meta.builder.simple.SimpleTableBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@AllArgsConstructor
public class SkipCreateOrAlterRDBDatabase implements RDBDatabase {
    private RDBDatabase target;

    private List<String> excludes;

    private SqlExecutor sqlExecutor;

    @Override
    public RDBDatabaseMetaData getMeta() {
        return target.getMeta();
    }

    @Override
    public <T> RDBTable<T> getTable(String name) {
        return target.getTable(name);
    }

    @Override
    public <T> RDBTable<T> createTable(RDBTableMetaData tableMetaData) throws SQLException {
        return target.createTable(tableMetaData);
    }

    @Override
    public <T> RDBTable<T> reloadTable(RDBTableMetaData tableMetaData) {
        return target.reloadTable(tableMetaData);
    }

    @Override
    public <T> RDBTable<T> alterTable(RDBTableMetaData tableMetaData) throws SQLException {
        return target.alterTable(tableMetaData);
    }

    @Override
    public boolean removeTable(String name) {
        return target.removeTable(name);
    }

    @Override
    public TableBuilder createOrAlter(String name) {
        if (excludes.contains(name)) {
            return new DoNotingTableBuilder();
        }
        return target.createOrAlter(name);
    }
}
