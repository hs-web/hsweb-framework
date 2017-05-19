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

    public DynamicDataSourceProxy(String id, DataSource proxy) throws SQLException {
        this.id = id;
        this.proxy = proxy;
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
                        e.printStackTrace();
                    }
                }
            }
        }

        return databaseType;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return proxy.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return proxy.getConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return proxy.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return proxy.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return proxy.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        proxy.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        proxy.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return proxy.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return proxy.getParentLogger();
    }
}
