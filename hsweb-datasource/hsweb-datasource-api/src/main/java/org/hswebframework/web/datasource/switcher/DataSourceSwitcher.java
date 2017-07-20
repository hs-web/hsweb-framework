package org.hswebframework.web.datasource.switcher;

/**
 * 动态数据源切换器,用于切换数据源操作
 *
 * @author zhouhao
 * @since  3.0
 * @see DefaultDataSourceSwitcher
 */
public interface DataSourceSwitcher {

    /**
     * 使用上一次调用的数据源
     */
    void useLast();

    /**
     * 选中参数(数据源ID)对应的数据源,如果数据源不存在,将使用默认数据源
     *
     * @param dataSourceId 数据源ID
     */
    void use(String dataSourceId);

    /**
     * 切换为默认数据源
     */
    void useDefault();

    /**
     * @return 当前选择的数据源ID, 如果为默认数据源则返回 {@code null}
     */
    String currentDataSourceId();

    /**
     * 重置切换记录,重置后,使用默认数据源
     */
    void reset();
}
