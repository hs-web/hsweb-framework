package org.hswebframework.web.datasource.switcher;

/**
 * TODO 完成注释
 *
 * @author zhouhao
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
     * @return 当前选择的数据源ID
     */
    String currentDataSourceId();

    /**
     * 重置切换记录
     */
    void reset();
}
