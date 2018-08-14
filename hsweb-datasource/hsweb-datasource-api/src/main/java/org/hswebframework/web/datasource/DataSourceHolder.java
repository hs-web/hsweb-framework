package org.hswebframework.web.datasource;

import org.hswebframework.web.datasource.exception.DataSourceNotFoundException;
import org.hswebframework.web.datasource.switcher.DataSourceSwitcher;
import org.hswebframework.web.datasource.switcher.DefaultDataSourceSwitcher;
import org.hswebframework.web.datasource.switcher.TableSwitcher;

/**
 * 用于操作动态数据源,如获取当前使用的数据源,使用switcher切换数据源等
 *
 * @author zhouhao
 * @since 3.0
 */
public final class DataSourceHolder {

    private static final DataSourceSwitcher defaultSwitcher = new DefaultDataSourceSwitcher();

    /**
     * 动态数据源切换器
     */
    static volatile DataSourceSwitcher dataSourceSwitcher = defaultSwitcher;
    /**
     * 动态数据源服务
     */
    static volatile DynamicDataSourceService dynamicDataSourceService;

    static volatile TableSwitcher tableSwitcher;

    public static void checkDynamicDataSourceReady() {
        if (dynamicDataSourceService == null) {
            throw new UnsupportedOperationException("dataSourceService not ready");
        }
    }

    /**
     * @return 动态数据源切换器
     */
    public static DataSourceSwitcher switcher() {
        return dataSourceSwitcher;
    }

    /**
     * @return 表切换器, 用于动态切换系统功能表
     */
    public static TableSwitcher tableSwitcher() {
        return tableSwitcher;
    }

    /**
     * @return 默认数据源
     */
    public static DynamicDataSource defaultDataSource() {
        checkDynamicDataSourceReady();
        return dynamicDataSourceService.getDefaultDataSource();
    }

    /**
     * 根据指定的数据源id获取动态数据源
     *
     * @param dataSourceId 数据源id
     * @return 动态数据源
     * @throws DataSourceNotFoundException 如果数据源不存在将抛出此异常
     */
    public static DynamicDataSource dataSource(String dataSourceId) {
        checkDynamicDataSourceReady();
        return dynamicDataSourceService.getDataSource(dataSourceId);
    }

    /**
     * @return 当前使用的数据源
     */
    public static DynamicDataSource currentDataSource() {
        String id = dataSourceSwitcher.currentDataSourceId();
        if (id == null) {
            return defaultDataSource();
        }
        checkDynamicDataSourceReady();
        return dynamicDataSourceService.getDataSource(id);
    }

    /**
     * @return 当前使用的数据源是否为默认数据源
     */
    public static boolean currentIsDefault() {
        return dataSourceSwitcher.currentDataSourceId() == null;
    }

    /**
     * 判断指定id的数据源是否存在
     *
     * @param id 数据源id {@link DynamicDataSource#getId()}
     * @return 数据源是否存在
     */
    public static boolean existing(String id) {
        try {
            checkDynamicDataSourceReady();
            return dynamicDataSourceService.getDataSource(id) != null;
        } catch (DataSourceNotFoundException e) {
            return false;
        }
    }

    /**
     * @return 当前使用的数据源是否存在
     */
    public static boolean currentExisting() {
        if (currentIsDefault()) {
            return true;
        }
        try {
            return currentDataSource() != null;
        } catch (DataSourceNotFoundException e) {
            return false;
        }
    }

    /**
     * @return 当前数据库类型
     */
    public static DatabaseType currentDatabaseType() {
        return currentDataSource().getType();
    }

    /**
     * @return 默认的数据库类型
     */
    public static DatabaseType defaultDatabaseType() {
        return defaultDataSource().getType();
    }
}
