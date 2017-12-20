package org.hswebframework.web.datasource.service;

import org.hswebframework.web.datasource.DynamicDataSource;
import org.hswebframework.web.datasource.DynamicDataSourceProxy;
import org.hswebframework.web.datasource.config.DynamicDataSourceConfig;
import org.hswebframework.web.datasource.config.DynamicDataSourceConfigRepository;
import org.hswebframework.web.datasource.config.InSpringDynamicDataSourceConfig;
import org.hswebframework.web.datasource.exception.DataSourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0
 */
public class InMemoryDynamicDataSourceService extends AbstractDynamicDataSourceService<InSpringDynamicDataSourceConfig> {


    @Autowired
    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public InMemoryDynamicDataSourceService(DynamicDataSourceConfigRepository<InSpringDynamicDataSourceConfig> repository, DynamicDataSource defaultDataSource) {
        super(repository, defaultDataSource);
    }

    public InMemoryDynamicDataSourceService(DynamicDataSourceConfigRepository<InSpringDynamicDataSourceConfig> repository, DataSource dataSource) throws SQLException {
        super(repository, dataSource);
    }

    @Deprecated
    public void registerDataSource(String id, DataSource dataSource) {
        InSpringDynamicDataSourceConfig config = new InSpringDynamicDataSourceConfig();
        config.setId(id);
        config.setName(id);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        dataSourceStore.put(id, new DataSourceCache(0L
                , new DynamicDataSourceProxy(id, dataSource), countDownLatch, config));
        countDownLatch.countDown();
    }

    @Override
    protected DataSourceCache createCache(InSpringDynamicDataSourceConfig config) {
        DataSource dataSource = applicationContext.getBean(config.getBeanName(), DataSource.class);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            return new DataSourceCache(config.hashCode(), new DynamicDataSourceProxy(config.getId(), dataSource), countDownLatch, config);

        } finally {
            countDownLatch.countDown();
        }

    }


}
