package org.hswebframework.web.service.form.simple;

import org.hswebframework.ezorm.rdb.RDBDatabase;
import org.hswebframework.ezorm.rdb.executor.SqlExecutor;
import org.hswebframework.ezorm.rdb.meta.parser.H2TableMetaParser;
import org.hswebframework.ezorm.rdb.meta.parser.MysqlTableMetaParser;
import org.hswebframework.ezorm.rdb.meta.parser.OracleTableMetaParser;
import org.hswebframework.ezorm.rdb.meta.parser.SqlServer2012TableMetaParser;
import org.hswebframework.ezorm.rdb.render.dialect.*;
import org.hswebframework.ezorm.rdb.simple.SimpleDatabase;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.datasource.DatabaseType;
import org.hswebframework.web.datasource.DynamicDataSource;
import org.hswebframework.web.service.form.DatabaseRepository;
import org.hswebframework.web.service.form.DynamicFormService;
import org.hswebframework.web.service.form.FormDeployService;
import org.hswebframework.web.service.form.events.DatabaseInitEvent;
import org.hswebframework.web.service.form.simple.cluster.ClusterDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@Service
public class SimpleDatabaseRepository implements DatabaseRepository {

    private volatile RDBDatabase defaultDatabase = null;
    private          SqlExecutor sqlExecutor     = null;

    @Value("${hsweb.dynamic-form.cluster:false}")
    private boolean cluster = false;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final Map<String, RDBDatabase>                                 repository            = new HashMap<>();
    private final Map<DatabaseType, Supplier<AbstractRDBDatabaseMetaData>> databaseMetaSuppliers = new EnumMap<>(DatabaseType.class);

    @Autowired
    public void setSqlExecutor(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }

    @PostConstruct
    public void init() {
        Objects.requireNonNull(sqlExecutor);
        databaseMetaSuppliers.put(DatabaseType.oracle, () -> {
            OracleRDBDatabaseMetaData metaData = new OracleRDBDatabaseMetaData();
            metaData.setParser(new OracleTableMetaParser(sqlExecutor));
            return metaData;
        });

        databaseMetaSuppliers.put(DatabaseType.mysql, () -> {
            MysqlRDBDatabaseMetaData metaData = new MysqlRDBDatabaseMetaData();
            metaData.setParser(new MysqlTableMetaParser(sqlExecutor));
            return metaData;
        });

        databaseMetaSuppliers.put(DatabaseType.h2, () -> {
            H2RDBDatabaseMetaData metaData = new H2RDBDatabaseMetaData();
            metaData.setParser(new H2TableMetaParser(sqlExecutor));
            return metaData;
        });
        databaseMetaSuppliers.put(DatabaseType.jtds_sqlserver, () -> {
            MSSQLRDBDatabaseMetaData metaData = new MSSQLRDBDatabaseMetaData();
            metaData.setParser(new SqlServer2012TableMetaParser(sqlExecutor));
            return metaData;
        });
        databaseMetaSuppliers.put(DatabaseType.sqlserver, databaseMetaSuppliers.get(DatabaseType.jtds_sqlserver));
    }

    @Override
    public RDBDatabase getDefaultDatabase() {
        if (defaultDatabase == null) {
            synchronized (this) {
                if (defaultDatabase == null) {
                    defaultDatabase = initDatabase(DataSourceHolder.defaultDatabaseType());
                }
            }
        }
        return defaultDatabase;
    }

    @Override
    public RDBDatabase getDatabase(String datasourceId) {
        DynamicDataSource dynamicDataSource = DataSourceHolder.dataSource(datasourceId);
        return repository.computeIfAbsent(datasourceId, id -> this.initDatabase(dynamicDataSource.getType()));
    }

    @Override
    public RDBDatabase getCurrentDatabase() {
        return repository
                .computeIfAbsent(DataSourceHolder.switcher().currentDataSourceId()
                        , id -> this.initDatabase(DataSourceHolder.currentDatabaseType()));
    }


    private RDBDatabase initDatabase(DatabaseType databaseType) {
        Supplier<AbstractRDBDatabaseMetaData> supplier = databaseMetaSuppliers.get(databaseType);
        Objects.requireNonNull(supplier, "database type" + databaseType + " is not support");
        AbstractRDBDatabaseMetaData metaData = supplier.get();

        SimpleDatabase database = cluster ?
                new ClusterDatabase(metaData, sqlExecutor) :
                new SimpleDatabase(metaData, sqlExecutor);
        database.setAutoParse(true);
        eventPublisher.publishEvent(new DatabaseInitEvent(database));
        return database;
    }
}
