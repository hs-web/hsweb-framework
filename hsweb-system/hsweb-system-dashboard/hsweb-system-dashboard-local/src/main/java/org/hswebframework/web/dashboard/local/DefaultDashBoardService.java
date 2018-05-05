package org.hswebframework.web.dashboard.local;

import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.dashboard.DashBoardConfigEntity;
import org.hswebframework.web.dashboard.DashBoardService;
import org.hswebframework.web.dashboard.local.dao.DashBoardConfigDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.EnableCacheGenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = "hsweb:dashboard-conf")
public class DefaultDashBoardService extends EnableCacheGenericEntityService<DashBoardConfigEntity, String> implements DashBoardService {

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
}
