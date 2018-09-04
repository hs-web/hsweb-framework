package org.hswebframework.web.service.datasource.simple;

import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.datasource.annotation.UseDefaultDataSource;
import org.hswebframework.web.datasource.config.DynamicDataSourceConfigRepository;
import org.hswebframework.web.entity.datasource.DataSourceConfigEntity;
import org.hswebframework.web.service.datasource.DataSourceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 1.0
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class InDBDataSourceRepository implements DynamicDataSourceConfigRepository<InDBDynamicDataSourceConfig> {
    private DataSourceConfigService dataSourceConfigService;

    public InDBDataSourceRepository(DataSourceConfigService dataSourceConfigService) {
        this.dataSourceConfigService = dataSourceConfigService;
    }

    public InDBDataSourceRepository() {
    }

    public void setDataSourceConfigService(DataSourceConfigService dataSourceConfigService) {
        this.dataSourceConfigService = dataSourceConfigService;
    }

    protected InDBDynamicDataSourceConfig convert(DataSourceConfigEntity entity) {
        if (null == entity) {
            return null;
        }
        Map<String, Object> config = entity.getProperties();
        if (config == null) {
            return null;
        }
        InDBDynamicDataSourceConfig target = FastBeanCopier.copy(config, InDBDynamicDataSourceConfig::new);
        target.setId(entity.getId());
        target.setName(entity.getName());
        target.setDescribe(entity.getDescribe());
        target.setProperties(config);
        return target;
    }

    @Override
    public List<InDBDynamicDataSourceConfig> findAll() {
        return dataSourceConfigService.select().stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public InDBDynamicDataSourceConfig findById(String dataSourceId) {
        return convert(dataSourceConfigService.selectByPk(dataSourceId));
    }

    @Override
    public InDBDynamicDataSourceConfig add(InDBDynamicDataSourceConfig config) {
        throw new UnsupportedOperationException("add AtomikosDataSourceConfig not support");
    }

    @Override
    public InDBDynamicDataSourceConfig remove(String dataSourceId) {
        throw new UnsupportedOperationException("remove datasource not support");
    }
}
