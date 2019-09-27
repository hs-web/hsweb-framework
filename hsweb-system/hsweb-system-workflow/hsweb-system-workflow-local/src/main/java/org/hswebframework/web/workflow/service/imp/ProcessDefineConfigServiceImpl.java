package org.hswebframework.web.workflow.service.imp;

import org.hswebframework.ezorm.rdb.operator.dml.query.SortOrder;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.EnableCacheGenericEntityService;
import org.hswebframework.web.workflow.dao.entity.ProcessDefineConfigEntity;
import org.hswebframework.web.workflow.service.ProcessDefineConfigService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Service
@CacheConfig(cacheNames = "process-config")
public class ProcessDefineConfigServiceImpl extends EnableCacheGenericEntityService<ProcessDefineConfigEntity, String>
        implements ProcessDefineConfigService {


    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }


    @Override
    @Caching(evict = {
            @CacheEvict(key = "'define-id:'+#entity.processDefineId"),
            @CacheEvict(key = "'define-key-latest:'+#entity.processDefineKey")
    })
    public String insert(ProcessDefineConfigEntity entity) {
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        entity.setStatus(DataStatus.STATUS_ENABLED);
        return super.insert(entity);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'define-id:'+#entity.processDefineId"),
            @CacheEvict(key = "'define-key-latest:'+#entity.processDefineKey")
    })
    public int updateByPk(String id, ProcessDefineConfigEntity entity) {
        entity.setUpdateTime(new Date());
        return super.updateByPk(id, entity);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'define-id:'+#entity.processDefineId"),
            @CacheEvict(key = "'define-key-latest:'+#entity.processDefineKey")
    })
    protected int updateByPk(ProcessDefineConfigEntity entity) {
        return super.updateByPk(entity);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'define-id:'+#entity.processDefineId"),
            @CacheEvict(key = "'define-key-latest:'+#entity.processDefineKey")
    })
    public String saveOrUpdate(ProcessDefineConfigEntity entity) {
        return super.saveOrUpdate(entity);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(key = "'define-id:'+#result.processDefineId"),
            @CacheEvict(key = "'define-key-latest:'+#result.processDefineKey")
    })
    public ProcessDefineConfigEntity deleteByPk(String id) {

        return super.deleteByPk(id);
    }

    @Override
    @Cacheable(key = "'define-id:'+#processDefineId")
    public ProcessDefineConfigEntity selectByProcessDefineId(String processDefineId) {
        return createQuery()
                .where("processDefineId", Objects.requireNonNull(processDefineId, "参数[processDefineId]不能为空"))
                .fetchOne()
                .orElse(null);
    }

    @Override
    @Cacheable(key = "'define-key-latest:'+#processDefineKey")
    public ProcessDefineConfigEntity selectByLatestProcessDefineKey(String processDefineKey) {
        return createQuery()
                .where("processDefineKey", Objects.requireNonNull(processDefineKey, "参数[processDefineKey]不能为空"))
                .and("status", DataStatus.STATUS_ENABLED)
                .orderBy(SortOrder.desc("updateTime"))
                .fetchOne().orElse(null);
    }
}
