package org.hswebframework.web.service.form.simple;

import org.hsweb.ezorm.core.Database;
import org.hsweb.ezorm.rdb.RDBDatabase;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.datasource.DatabaseType;
import org.hswebframework.web.datasource.DynamicDataSource;
import org.hswebframework.web.service.form.DatabaseRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhouhao on 2017/7/2.
 */
public class SimpleDatabaseRepository implements DatabaseRepository {

    private RDBDatabase defaultDatabase;

    private Map<String,RDBDatabase> repository = new HashMap<>();

    @Override
    public RDBDatabase getDefaultDatabase() {
        if(defaultDatabase==null){
            synchronized (this){
                if(defaultDatabase==null){
                    defaultDatabase=initDatabase(DataSourceHolder.defaultDatabaseType());
                }
            }
        }
        return defaultDatabase;
    }

    @Override
    public RDBDatabase getDatabase(String datasourceId) {
        DynamicDataSource dynamicDataSource =DataSourceHolder.dataSource(datasourceId);
        return repository.computeIfAbsent(datasourceId,id->this.initDatabase(dynamicDataSource.getType()));
    }

    @Override
    public RDBDatabase getCurrentDatabase() {
        return repository
                .computeIfAbsent(DataSourceHolder.switcher().currentDataSourceId()
                        ,id->this.initDatabase(DataSourceHolder.currentDatabaseType()));
    }


    private RDBDatabase initDatabase(DatabaseType databaseType){

        return  null;
    }
}
