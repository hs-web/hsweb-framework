package org.hswebframework.web.datasource;

import javax.sql.DataSource;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface DynamicDataSource extends DataSource {
    String getId();

    DatabaseType getType();

}
