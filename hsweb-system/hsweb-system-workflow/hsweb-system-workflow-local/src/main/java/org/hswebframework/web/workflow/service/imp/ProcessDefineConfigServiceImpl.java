package org.hswebframework.web.workflow.service.imp;

import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.EnableCacheGenericEntityService;
import org.hswebframework.web.workflow.dao.ProcessDefineConfigDao;
import org.hswebframework.web.workflow.dao.entity.ProcessDefineConfigEntity;
import org.hswebframework.web.workflow.service.ProcessDefineConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
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

    @Autowired
    private ProcessDefineConfigDao processDefineConfigDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public CrudDao<ProcessDefineConfigEntity, String> getDao() {
        return processDefineConfigDao;
    }


    @Override
    @Caching(evict = {
            @CacheEvict(key = "'define-id:'+#entity.processDefineId"),
            @CacheEvict(key = "'define-key-latest:'+#entity.processDefineKey", condition = "#entity.status==1")
    })
    public String insert(ProcessDefineConfigEntity entity) {
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
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
            @CacheEvict(key = "'define-id:'+#result.processDefineId"),
            @CacheEvict(key = "'define-key-latest:'+#result.processDefineKey")
    })
    public ProcessDefineConfigEntity deleteByPk(String id) {

        return super.deleteByPk(id);
    }

    @Override
    @CacheEvict(key = "'define-id:'+#processDefineId")
    public ProcessDefineConfigEntity selectByProcessDefineId(String processDefineId) {
        return createQuery()
                .where("processDefineId", Objects.requireNonNull(processDefineId, "参数[processDefineId]不能为空"))
                .single();
    }

    @Override
    @CacheEvict(key = "'define-key-latest:'+#processDefineKey")
    public ProcessDefineConfigEntity selectByLatestProcessDefineKey(String processDefineKey) {
        return createQuery()
                .where("processDefineKey", Objects.requireNonNull(processDefineKey, "参数[processDefineKey]不能为空"))
                .and("status", DataStatus.STATUS_ENABLED)
                .orderByDesc("updateTime")
                .single();
    }
}
