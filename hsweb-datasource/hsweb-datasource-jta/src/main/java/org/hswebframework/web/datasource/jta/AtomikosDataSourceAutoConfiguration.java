package org.hswebframework.web.datasource.jta;

import org.hswebframework.web.datasource.DynamicDataSourceService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Configuration
public class AtomikosDataSourceAutoConfiguration {

    //默认数据源
    @Bean(initMethod = "init", destroyMethod = "destroy", value = "defaultDataSource")
    @Primary
    public AtomikosDataSourceBean defaultDataSource() {
        return new AtomikosDataSourceBean();
    }

    @ConditionalOnMissingBean(JtaDataSourceStore.class)
    @Bean
    public MemoryJtaDataSourceStore memoryJtaDataSourceStore() {
        return new MemoryJtaDataSourceStore();
    }

    @Bean
    @ConditionalOnMissingBean(DynamicDataSourceService.class)
    public JtaDynamicDataSourceService jtaDynamicDataSourceService(JtaDataSourceStore jtaDataSourceStore, DataSource dataSource) throws SQLException {
        return new JtaDynamicDataSourceService(jtaDataSourceStore, dataSource);
    }
}
