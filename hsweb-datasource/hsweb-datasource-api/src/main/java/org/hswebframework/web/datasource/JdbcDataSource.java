package org.hswebframework.web.datasource;

import javax.sql.DataSource;

/**
 * 动态数据源
 *
 * @author zhouhao
 * @since 3.0
 */
public interface JdbcDataSource extends DynamicDataSource<DataSource> {

    @Override
    DataSource getNative();
}
