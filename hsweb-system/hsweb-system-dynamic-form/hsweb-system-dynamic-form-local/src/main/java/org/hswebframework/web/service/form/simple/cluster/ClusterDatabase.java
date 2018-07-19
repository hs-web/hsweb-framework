package org.hswebframework.web.service.form.simple.cluster;

import org.hswebframework.ezorm.rdb.RDBTable;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.ezorm.rdb.meta.RDBDatabaseMetaData;
import org.hswebframework.ezorm.rdb.meta.RDBTableMetaData;
import org.hswebframework.ezorm.rdb.simple.SimpleDatabase;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public class ClusterDatabase extends SimpleDatabase {
    public ClusterDatabase(RDBDatabaseMetaData metaData, SqlExecutor sqlExecutor) {
        super(metaData, sqlExecutor);
    }

    private volatile Consumer<RDBTableMetaData> versionChanged;

    private volatile Function<RDBTableMetaData, Long> latestVersionGetter;

    public void setVersionChanged(Consumer<RDBTableMetaData> versionChanged) {
        this.versionChanged = versionChanged;
    }

    public void setLatestVersionGetter(Function<RDBTableMetaData, Long> latestVersionGetter) {
        this.latestVersionGetter = latestVersionGetter;
    }

    @Override
    public <T> RDBTable<T> getTable(String name) {
        RDBTable<T> table = super.getTable(name);
        if (versionChanged == null || latestVersionGetter == null) {
            return table;
        }
        if (table != null) {
            long version = table.getMeta().getProperty("version", -1L).getValue();
            if (version == -1L) {
                return table;
            }
            if (version != latestVersionGetter.apply(table.getMeta())) {
                versionChanged.accept(table.getMeta());
            }
            return super.getTable(name);
        }
        return null;
    }
}
