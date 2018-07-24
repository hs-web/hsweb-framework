package org.hswebframework.web.workflow.service.imp;

import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.EnableCacheGenericEntityService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.workflow.dao.ActivityConfigDao;
import org.hswebframework.web.workflow.dao.entity.ActivityConfigEntity;
import org.hswebframework.web.workflow.service.ActivityConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Service
@CacheConfig(cacheNames = "process-activity-config")
public class ActivityConfigServiceImpl extends GenericEntityService<ActivityConfigEntity, String>
        implements ActivityConfigService {

    @Autowired
    private ActivityConfigDao activityConfigDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public CrudDao<ActivityConfigEntity, String> getDao() {
        return activityConfigDao;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'define-id:'+#entity.processDefineId+'-'+#entity.activityId"),
            @CacheEvict(key = "'define-key:'+#entity.processDefineKey+'-'+#entity.activityId")
    })
    public String insert(ActivityConfigEntity entity) {
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        entity.setStatus(DataStatus.STATUS_ENABLED);
        return super.insert(entity);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'define-id:'+#entity.processDefineId+'-'+#entity.activityId"),
            @CacheEvict(key = "'define-key:'+#entity.processDefineKey+'-'+#entity.activityId")
    })
    public int updateByPk(String pk, ActivityConfigEntity entity) {
        entity.setUpdateTime(new Date());
        return super.updateByPk(pk, entity);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'define-id:'+#entity.processDefineId+'-'+#entity.activityId"),
            @CacheEvict(key = "'define-key:'+#entity.processDefineKey+'-'+#entity.activityId")
    })
    protected int updateByPk(ActivityConfigEntity entity) {
        return super.updateByPk(entity);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'define-id:'+#entity.processDefineId+'-'+#entity.activityId"),
            @CacheEvict(key = "'define-key:'+#entity.processDefineKey+'-'+#entity.activityId")
    })
    public String saveOrUpdate(ActivityConfigEntity entity) {
        return super.saveOrUpdate(entity);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'define-id:'+#result.processDefineId+'-'+#result.activityId", condition = "#result!=null"),
            @CacheEvict(key = "'define-key:'+#result.processDefineKey+'-'+#result.activityId", condition = "#result!=null")
    })
    public ActivityConfigEntity deleteByPk(String id) {
        return super.deleteByPk(id);
    }

    @Override
    @Cacheable(key = "'define-id:'+#processDefineId+'-'+#activityId")
    public ActivityConfigEntity selectByProcessDefineIdAndActivityId(String processDefineId, String activityId) {
        return createQuery()
                .where("processDefineId", processDefineId)
                .and("activityId", activityId)
                .single();
    }

    @Override
    @Cacheable(key = "'define-key:'+#processDefineKey+'-'+#activityId")
    public ActivityConfigEntity selectByProcessDefineKeyAndActivityId(String processDefineKey, String activityId) {
        return createQuery()
                .where("processDefineKey", processDefineKey)
                .and("activityId", activityId)
                .single();
    }
}
