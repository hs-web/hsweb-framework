package org.hswebframework.web.dashboard.local;

import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.dashboard.DashBoardConfigEntity;
import org.hswebframework.web.dashboard.DashBoardService;
import org.hswebframework.web.dashboard.local.dao.DashBoardConfigDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.EnableCacheAllEvictGenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "hsweb:dashboard-conf")
public class DefaultDashBoardService extends EnableCacheAllEvictGenericEntityService<DashBoardConfigEntity, String> implements DashBoardService {

    @Autowired
    private DashBoardConfigDao dashBoardConfigDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public CrudDao<DashBoardConfigEntity, String> getDao() {
        return dashBoardConfigDao;
    }

    @Cacheable(key = "'all-defaults'")
    public List<DashBoardConfigEntity> selectAllDefaults() {
        return createQuery().where("defaultConfig", true).or().isNull("defaultConfig").listNoPaging();
    }
}
