package org.hswebframework.web.datasource.service;

import org.hswebframework.web.datasource.DynamicDataSource;
import org.hswebframework.web.datasource.DynamicDataSourceProxy;
import org.hswebframework.web.datasource.exception.DataSourceNotFoundException;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhouhao
 * @since 1.0
 */
public class InMemoryDynamicDataSourceService extends AbstractDynamicDataSourceService {
    public InMemoryDynamicDataSourceService(DynamicDataSource defaultDataSource) {
        super(defaultDataSource);
    }

    private Map<String, DataSourceCache> dataSourceMap = new HashMap<>();

    public void registerDataSource(String id, DataSource dataSource) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        dataSourceMap.put(id, new DataSourceCache(0L
                , new DynamicDataSourceProxy(id, dataSource), countDownLatch));
        countDownLatch.countDown();
    }

    @Override
    protected int getHash(String id) {
        return 0;
    }

    @Override
    protected DataSourceCache createCache(String id) {
        DataSourceCache cache = dataSourceMap.get(id);
        if (cache == null) {
            throw new DataSourceNotFoundException(id);
        }
        return cache;
    }

}
