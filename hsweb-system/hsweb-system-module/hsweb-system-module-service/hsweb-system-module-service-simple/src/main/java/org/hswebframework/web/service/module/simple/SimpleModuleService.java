package org.hswebframework.web.service.module.simple;

import org.hswebframework.web.dao.module.ModuleDao;
import org.hswebframework.web.entity.module.ModuleEntity;
import org.hswebframework.web.service.EnableCacheGenericEntityService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.module.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("moduleService")
@CacheConfig(cacheNames = "system-module")
public class SimpleModuleService extends EnableCacheGenericEntityService<ModuleEntity, String>
        implements ModuleService {
    @Autowired
    private ModuleDao moduleDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public ModuleDao getDao() {
        return moduleDao;
    }

}
