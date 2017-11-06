package org.hswebframework.web.service.organizational.simple;

import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.dao.organizational.DistrictDao;
import org.hswebframework.web.entity.organizational.DistrictEntity;
import org.hswebframework.web.entity.organizational.OrganizationalEntity;
import org.hswebframework.web.service.AbstractTreeSortService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.organizational.DistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("districtService")
@CacheConfig(cacheNames = "district")
public class SimpleDistrictService extends AbstractTreeSortService<DistrictEntity, String>
        implements DistrictService {
    @Autowired
    private DistrictDao districtDao;

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
        return super.insert(entity);
    }

    @Override
    @CacheEvict(allEntries = true)
    public int updateByPk(String id, DistrictEntity entity) {
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
    public int deleteByPk(String id) {
        return super.deleteByPk(id);
    }

    @Override
    @Cacheable(key = "'code:'+#code")
    public DistrictEntity selectByCode(String code) {
        return createQuery().where(DistrictEntity.code, code).single();
    }

    @Override
    @Cacheable(key = "'all'")
    public List<DistrictEntity> select(Entity param) {
        return createQuery().where().orderByAsc(DistrictEntity.sortIndex).listNoPaging();
    }

    @Override
    @CacheEvict(allEntries = true)
    public int updateBatch(Collection<DistrictEntity> data) {
        return super.updateBatch(data);
    }

    @Override
    @Cacheable(key = "'all'")
    public List<DistrictEntity> select() {
        return super.select();
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
    }

    @Override
    @CacheEvict(allEntries = true)
    public void enable(String id) {
        Objects.requireNonNull(id);
        createUpdate()
                .set(DistrictEntity.status, DataStatus.STATUS_ENABLED)
                .where(DistrictEntity.id, id)
                .exec();
    }
}
