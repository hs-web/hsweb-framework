package org.hswebframework.web.datasource.service;

import org.hswebframework.web.datasource.DynamicDataSource;
import org.hswebframework.web.datasource.DynamicDataSourceProxy;
import org.hswebframework.web.datasource.DynamicDataSourceService;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 */
public abstract class AbstractDynamicDataSourceService implements DynamicDataSourceService {
    protected final Map<String, DataSourceCache> dataSourceStore = new ConcurrentHashMap<>(32);

    private final DynamicDataSource defaultDataSource;

    public AbstractDynamicDataSourceService(DynamicDataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    public AbstractDynamicDataSourceService(DataSource dataSource) throws SQLException {
        this(dataSource instanceof DynamicDataSource ? (DynamicDataSource) dataSource : new DynamicDataSourceProxy(null, dataSource));
    }

    @PreDestroy
    public void destroy() {
        dataSourceStore.values().forEach(DataSourceCache::closeDataSource);
    }

    @Override
    public DynamicDataSource getDataSource(String dataSourceId) {
        DataSourceCache cache = dataSourceStore.get(dataSourceId);
        if (cache == null) {
            cache = createCache(dataSourceId);
            dataSourceStore.put(dataSourceId, cache);
            return cache.getDataSource();
        }
        if (cache.getHash() != getHash(dataSourceId)) {
            dataSourceStore.remove(dataSourceId);
            cache.closeDataSource();
            //重新获取
            return getDataSource(dataSourceId);
        }
        return cache.getDataSource();
    }

    @Override
    public DynamicDataSource getDefaultDataSource() {
        return defaultDataSource;
    }

    protected abstract int getHash(String id);

    protected abstract DataSourceCache createCache(String id);

    public DataSourceCache removeCache(String id) {
        return dataSourceStore.remove(id);
    }
}
