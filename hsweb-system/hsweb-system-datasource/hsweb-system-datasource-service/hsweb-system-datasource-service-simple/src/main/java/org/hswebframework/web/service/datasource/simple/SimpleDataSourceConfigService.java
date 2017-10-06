package org.hswebframework.web.service.datasource.simple;

import org.hswebframework.web.dao.datasource.DataSourceConfigDao;
import org.hswebframework.web.entity.datasource.DataSourceConfigEntity;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.datasource.DataSourceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("dataSourceConfigService")
public class SimpleDataSourceConfigService extends GenericEntityService<DataSourceConfigEntity, String>
        implements DataSourceConfigService {
    @Autowired
    private DataSourceConfigDao dataSourceConfigDao;
   @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }
    @Override
    public DataSourceConfigDao getDao() {
        return dataSourceConfigDao;
    }

}
