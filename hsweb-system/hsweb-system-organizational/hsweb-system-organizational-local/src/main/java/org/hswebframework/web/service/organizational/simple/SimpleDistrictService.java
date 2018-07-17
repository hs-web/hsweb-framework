package org.hswebframework.web.service.organizational.simple;

import org.hswebframework.web.BusinessException;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.dao.organizational.DistrictDao;
import org.hswebframework.web.dao.organizational.OrganizationalDao;
import org.hswebframework.web.entity.organizational.DistrictEntity;
import org.hswebframework.web.entity.organizational.OrganizationalEntity;
import org.hswebframework.web.service.AbstractTreeSortService;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.EnableCacheAllEvictTreeSortService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.organizational.DistrictService;
import org.hswebframework.web.service.organizational.event.ClearPersonCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.hswebframework.web.service.DefaultDSLQueryService.*;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("districtService")
@CacheConfig(cacheNames = "district")
public class SimpleDistrictService extends EnableCacheAllEvictTreeSortService<DistrictEntity, String>
        implements DistrictService {
    @Autowired
    private DistrictDao districtDao;

    @Autowired
    private OrganizationalDao organizationalDao;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public DistrictDao getDao() {
        return districtDao;
    }

    @Override
    @CacheEvict(allEntries = true)
    public String insert(DistrictEntity entity) {
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.insert(entity);
    }

    @Override
    @CacheEvict(allEntries = true)
    public int updateByPk(String id, DistrictEntity entity) {
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.updateByPk(id, entity);
    }

    @Override
    @Cacheable(key = "'ids:'+(#id==null?0:#id.hashCode())")
    public List<DistrictEntity> selectByPk(List<String> id) {
        return super.selectByPk(id);
    }

    @Override
    @Cacheable(key = "'id:'+#id")
    public DistrictEntity selectByPk(String id) {
        return super.selectByPk(id);
    }

    @Override
    @CacheEvict(allEntries = true)
    public DistrictEntity deleteByPk(String id) {
        if (DefaultDSLQueryService.createQuery(organizationalDao).where(OrganizationalEntity.districtId, id).total() > 0) {
            throw new BusinessException("行政区域下存在机构信息,无法删除!");
        }
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.deleteByPk(id);
    }

    @Override
    @Cacheable(key = "'code:'+#code")
    public DistrictEntity selectByCode(String code) {
        return createQuery().where(DistrictEntity.code, code).single();
    }

    @Override
    @CacheEvict(allEntries = true)
    public int updateBatch(Collection<DistrictEntity> data) {
        return super.updateBatch(data);
    }

    @Override
    @Cacheable(key = "'all'")
    public List<DistrictEntity> select() {
        return createQuery().where().orderByAsc(DistrictEntity.sortIndex).listNoPaging();
    }

    @Override
    @CacheEvict(allEntries = true)
    public int updateByPk(List<DistrictEntity> data) {
        return super.updateByPk(data);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void disable(String id) {
        Objects.requireNonNull(id);
        createUpdate()
                .set(DistrictEntity.status, DataStatus.STATUS_DISABLED)
                .where(DistrictEntity.id, id)
                .exec();
        publisher.publishEvent(new ClearPersonCacheEvent());
    }

    @Override
    @CacheEvict(allEntries = true)
    public void enable(String id) {
        Objects.requireNonNull(id);
        createUpdate()
                .set(DistrictEntity.status, DataStatus.STATUS_ENABLED)
                .where(DistrictEntity.id, id)
                .exec();
        publisher.publishEvent(new ClearPersonCacheEvent());
    }
}
