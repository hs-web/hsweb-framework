package org.hswebframework.web.datasource;

import org.hswebframework.web.datasource.switcher.DataSourceSwitcher;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public final class DataSourceHolder {

    static DataSourceSwitcher dataSourceSwitcher;

    static DynamicDataSourceService dynamicDataSourceService;

    public static DataSourceSwitcher switcher() {
        return dataSourceSwitcher;
    }

    public static DynamicDataSource getDefaultDataSource() {
        return dynamicDataSourceService.getDefaultDataSource();
    }

    public static DynamicDataSource getActiveDataSource() {
        String id = dataSourceSwitcher.currentDataSourceId();
        if (id == null) return getDefaultDataSource();
        return dynamicDataSourceService.getDataSource(id);
    }

    public static DatabaseType getActiveDatabaseType() {
        return getActiveDataSource().getType();
    }

    public static DatabaseType getDefaultDatabaseType() {
        return getDefaultDataSource().getType();
    }
}
