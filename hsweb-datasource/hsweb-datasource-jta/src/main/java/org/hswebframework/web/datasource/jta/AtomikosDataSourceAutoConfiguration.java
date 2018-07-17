package org.hswebframework.web.datasource.jta;

import org.hswebframework.web.datasource.DynamicDataSourceAutoConfiguration;
import org.hswebframework.web.datasource.DynamicDataSourceService;
import org.hswebframework.web.datasource.config.DynamicDataSourceConfigRepository;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author zhouhao
 */
@Configuration
@AutoConfigureBefore(DynamicDataSourceAutoConfiguration.class)
public class AtomikosDataSourceAutoConfiguration {

    //默认数据源
    @Bean(initMethod = "init", destroyMethod = "destroy", value = "datasource")
    @Primary
    public AtomikosDataSourceBean datasource() {
        return new AtomikosDataSourceBean();
    }

    @ConditionalOnMissingBean(JtaDataSourceRepository.class)
    @Bean
    public InMemoryAtomikosDataSourceRepository memoryJtaDataSourceStore() {
        return new InMemoryAtomikosDataSourceRepository();
    }

    @Bean
    @Primary
    public DynamicDataSourceService jtaDynamicDataSourceService(DynamicDataSourceConfigRepository<AtomikosDataSourceConfig> repository
            , DataSource dataSource) throws SQLException {
        return new JtaDynamicDataSourceService(repository, dataSource);
    }

}
