package org.hswebframework.web.datasource.jta;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhouhao
 */
@ConfigurationProperties(prefix = "hsweb.datasource")
public class InMemoryAtomikosDataSourceRepository implements JtaDataSourceRepository {

    @Getter
    @Setter
    private Map<String, AtomikosDataSourceConfig> jta = new HashMap<>();


    @PostConstruct
    public void init() {
        jta.forEach((id, config) -> {
            if (config.getId() == null) {
                config.setId(id);
            } else if (!config.getId().equals(id)) {
                jta.put(config.getId(), config);
            }
        });
    }

    @Override
    public List<AtomikosDataSourceConfig> findAll() {
        return new ArrayList<>(jta.values());
    }

    @Override
    public AtomikosDataSourceConfig findById(String dataSourceId) {
        return jta.get(dataSourceId);
    }

    @Override
    public AtomikosDataSourceConfig add(AtomikosDataSourceConfig config) {
        return jta.put(config.getId(), config);
    }

    @Override
    public AtomikosDataSourceConfig remove(String dataSourceId) {
        return jta.remove(dataSourceId);
    }
}
