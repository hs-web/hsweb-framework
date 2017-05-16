package org.hswebframework.web.datasource;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface DynamicDataSourceService {
    DynamicDataSource getDataSource(String dataSourceId);

    DynamicDataSource getDefaultDataSource();
}
