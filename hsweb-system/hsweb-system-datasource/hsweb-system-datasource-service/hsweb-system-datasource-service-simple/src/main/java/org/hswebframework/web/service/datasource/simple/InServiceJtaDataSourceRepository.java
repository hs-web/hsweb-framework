package org.hswebframework.web.service.datasource.simple;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.datasource.jta.AtomikosDataSourceConfig;
import org.hswebframework.web.datasource.jta.JtaDataSourceRepository;
import org.hswebframework.web.entity.datasource.DataSourceConfigEntity;
import org.hswebframework.web.service.datasource.DataSourceConfigService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0
 */
public class InServiceJtaDataSourceRepository implements JtaDataSourceRepository {
    private DataSourceConfigService dataSourceConfigService;

    private EntityFactory entityFactory;

    @Autowired
    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    @Autowired
    public void setDataSourceConfigService(DataSourceConfigService dataSourceConfigService) {
        this.dataSourceConfigService = dataSourceConfigService;
    }

    protected AtomikosDataSourceConfig convert(DataSourceConfigEntity entity) {
        if (null == entity) {
            return null;
        }
        Map<String, Object> config = entity.getProperties();
        if (config == null) {
            return null;
        }
        Object xaProperties = config.get("xaProperties");
        Properties properties = new Properties();

        if (xaProperties instanceof String) {
            xaProperties = JSON.parseObject(((String) xaProperties));
        }
        if (xaProperties instanceof Map) {
            properties.putAll(((Map) xaProperties));
        }
        config.remove("xaProperties");
        AtomikosDataSourceConfig target = entityFactory.copyProperties(config, new AtomikosDataSourceConfig());
        target.setId(entity.getId());
        target.setName(entity.getName());
        target.setDescribe(entity.getDescribe());
        target.setXaProperties(properties);
        return target;
    }

    @Override
    public List<AtomikosDataSourceConfig> findAll() {
        return dataSourceConfigService.select().stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public AtomikosDataSourceConfig findById(String dataSourceId) {
        return convert(dataSourceConfigService.selectByPk(dataSourceId));
    }

    @Override
    public AtomikosDataSourceConfig add(AtomikosDataSourceConfig config) {
        throw new UnsupportedOperationException("add AtomikosDataSourceConfig not support");
    }

    @Override
    public AtomikosDataSourceConfig remove(String dataSourceId) {
        throw new UnsupportedOperationException("remove datasource not support");
    }
}
