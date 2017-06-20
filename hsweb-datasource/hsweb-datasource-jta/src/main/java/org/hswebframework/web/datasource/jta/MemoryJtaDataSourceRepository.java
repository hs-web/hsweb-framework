package org.hswebframework.web.datasource.jta;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@ConfigurationProperties(prefix = "hsweb.dynamic")
public class MemoryJtaDataSourceRepository implements JtaDataSourceRepository {
    private Map<String, AtomikosDataSourceConfig> datasource = new HashMap<>();

    @Override
    public AtomikosDataSourceConfig getConfig(String id) {
        return datasource.get(id);
    }

    public Map<String, AtomikosDataSourceConfig> getDatasource() {
        return datasource;
    }

    public void setDatasource(Map<String, AtomikosDataSourceConfig> datasource) {
        this.datasource = datasource;
    }
}
