package org.hswebframework.web.datasource.jta;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhouhao
 */
@ConfigurationProperties(prefix = "hsweb.dynamic")
public class InMemoryAtomikosDataSourceRepository implements JtaDataSourceRepository {
    private Map<String, AtomikosDataSourceConfig> datasource = new HashMap<>();

    public Map<String, AtomikosDataSourceConfig> getDatasource() {
        return datasource;
    }

    public void setDatasource(Map<String, AtomikosDataSourceConfig> datasource) {
        this.datasource = datasource;
    }

    @PostConstruct
    public void init() {
        datasource.forEach((id, config) -> {
            if (config.getId() == null) {
                config.setId(id);
            } else if (!config.getId().equals(id)) {
                datasource.put(config.getId(), config);
            }
        });
    }

    @Override
    public List<AtomikosDataSourceConfig> findAll() {
        return new ArrayList<>(datasource.values());
    }

    @Override
    public AtomikosDataSourceConfig findById(String dataSourceId) {
        return datasource.get(dataSourceId);
    }

    @Override
    public AtomikosDataSourceConfig add(AtomikosDataSourceConfig config) {
        return datasource.put(config.getId(), config);
    }

    @Override
    public AtomikosDataSourceConfig remove(String dataSourceId) {
        return datasource.remove(dataSourceId);
    }
}
