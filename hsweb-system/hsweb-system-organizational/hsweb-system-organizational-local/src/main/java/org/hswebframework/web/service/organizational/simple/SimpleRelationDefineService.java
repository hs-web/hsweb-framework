package org.hswebframework.web.service.organizational.simple;

import org.hswebframework.web.dao.organizational.RelationDefineDao;
import org.hswebframework.web.entity.organizational.RelationDefineEntity;
import org.hswebframework.web.service.EnableCacheGenericEntityService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.organizational.RelationDefineService;
import org.hswebframework.web.service.organizational.event.ClearPersonCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("relationDefineService")
@CacheConfig(cacheNames = "hsweb-relation-define")
public class SimpleRelationDefineService extends EnableCacheGenericEntityService<RelationDefineEntity, String>
        implements RelationDefineService {
    @Autowired
    private RelationDefineDao relationDefineDao;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public RelationDefineDao getDao() {
        return relationDefineDao;
    }

    @Override
    public String insert(RelationDefineEntity entity) {
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.insert(entity);
    }

    @Override
    public RelationDefineEntity deleteByPk(String pk) {
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.deleteByPk(pk);
    }

    @Override
    public int updateByPk(String pk, RelationDefineEntity entity) {
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.updateByPk(pk, entity);
    }

}
