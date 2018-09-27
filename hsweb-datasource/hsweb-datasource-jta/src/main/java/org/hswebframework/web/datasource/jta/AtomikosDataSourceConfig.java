package org.hswebframework.web.datasource.jta;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.datasource.config.DynamicDataSourceConfig;

import javax.sql.XADataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author zhouhao
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class AtomikosDataSourceConfig extends DynamicDataSourceConfig {
    private static final long       serialVersionUID        = 5588085000663972571L;
    private              int        minPoolSize             = 5;
    private              int        maxPoolSize             = 200;
    private              int        borrowConnectionTimeout = 60;
    private              int        reapTimeout             = 0;
    private              int        maxIdleTime             = 60;
    private              int        maintenanceInterval     = 60;
    private              int        defaultIsolationLevel   = -1;
    private              String     xaDataSourceClassName   = null;
    private              int        loginTimeout            = 0;
    private              String     testQuery               = null;
    private              int        maxLifetime             = 0;
    private              Properties xaProperties            = null;
    //初始化超时时间
    private              int        initTimeout             = 10;

    @SneakyThrows
    public void putProperties(AtomikosDataSourceBean atomikosDataSourceBean) {

        if (null != xaProperties) {
            xaProperties.entrySet().forEach(entry -> entry.setValue(String.valueOf(entry.getValue())));
        }
        //fix #87
        XADataSource dataSource = (XADataSource) Class.forName(getXaDataSourceClassName()).newInstance();
        FastBeanCopier.copy(xaProperties, dataSource);
        atomikosDataSourceBean.setXaDataSource(dataSource);

        atomikosDataSourceBean.setXaDataSourceClassName(getXaDataSourceClassName());
        atomikosDataSourceBean.setBorrowConnectionTimeout(getBorrowConnectionTimeout());
        if (loginTimeout != 0) {
            try {
                atomikosDataSourceBean.setLoginTimeout(getLoginTimeout());
            } catch (SQLException e) {
                log.warn(e.getMessage(), e);
            }
        }
        atomikosDataSourceBean.setMaxIdleTime(getMaxIdleTime());
        atomikosDataSourceBean.setMaxPoolSize(getMaxPoolSize());
        atomikosDataSourceBean.setMinPoolSize(getMinPoolSize());
        atomikosDataSourceBean.setDefaultIsolationLevel(getDefaultIsolationLevel());
        atomikosDataSourceBean.setMaintenanceInterval(getMaintenanceInterval());
        atomikosDataSourceBean.setReapTimeout(getReapTimeout());
        atomikosDataSourceBean.setTestQuery(getTestQuery());
        atomikosDataSourceBean.setXaProperties(getXaProperties());
        atomikosDataSourceBean.setMaxLifetime(getMaxLifetime());
    }
}
