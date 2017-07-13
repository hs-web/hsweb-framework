package org.hswebframework.web.datasource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * 动态数据源代理,将数据源代理为动态数据源
 *
 * @author zhouhao
 * @since 3.0
 */
public class DynamicDataSourceProxy implements DynamicDataSource {

    private String id;

    private DatabaseType databaseType;

    private DataSource proxy;

    public DynamicDataSourceProxy(String id, DatabaseType databaseType, DataSource proxy) {
        this.id = id;
        this.databaseType = databaseType;
        this.proxy = proxy;
    }

    public DynamicDataSourceProxy(String id, DataSource proxy) {
        this.id = id;
        this.proxy = proxy;
    }

    @Override
    public DataSource getNative() {
        return proxy;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public DatabaseType getType() {
        if (databaseType == null) {
            synchronized (this) {
                if (databaseType == null) {
                    try {
                        try (Connection connection = proxy.getConnection()) {
                            databaseType = DatabaseType.fromJdbcUrl(connection.getMetaData().getURL());
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return databaseType;
    }
}
