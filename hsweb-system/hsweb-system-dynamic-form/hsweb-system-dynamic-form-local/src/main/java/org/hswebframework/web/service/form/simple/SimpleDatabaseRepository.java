package org.hswebframework.web.service.form.simple;

import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.datasource.DatabaseType;
import org.hswebframework.web.datasource.DynamicDataSource;
import org.hswebframework.web.service.form.DatabaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class SimpleDatabaseRepository implements DatabaseRepository {

    @Autowired
    private DatabaseOperator defaultDatabase ;

    private SyncSqlExecutor sqlExecutor = null;

    @Value("${hsweb.dynamic-form.cluster:false}")
    private boolean cluster = false;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final Map<String, DatabaseOperator> repository = new HashMap<>();

    @Autowired
    public void setSqlExecutor(SyncSqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }

    @PostConstruct
    public void init() {
        Objects.requireNonNull(sqlExecutor);
    }

    @Override
    public DatabaseOperator getDefaultDatabase(String databaseName) {
        return repository.computeIfAbsent("DEFAULT." + databaseName, id -> this.initDatabase(DataSourceHolder.defaultDatabaseType(), databaseName));
    }

    @Override
    public DatabaseOperator getDatabase(String datasourceId, String databaseName) {
        DynamicDataSource dynamicDataSource = DataSourceHolder.dataSource(datasourceId);
        return repository.computeIfAbsent(datasourceId + "." + databaseName, id -> this.initDatabase(dynamicDataSource.getType(), databaseName));
    }

    @Override
    public DatabaseOperator getCurrentDatabase() {
        return repository
                .computeIfAbsent(DataSourceHolder.switcher().currentDataSourceId()
                        , id -> this.initDatabase(DataSourceHolder.currentDatabaseType(), null));
    }


    private DatabaseOperator initDatabase(DatabaseType databaseType, String databaseName) {

      return defaultDatabase;
    }
}
