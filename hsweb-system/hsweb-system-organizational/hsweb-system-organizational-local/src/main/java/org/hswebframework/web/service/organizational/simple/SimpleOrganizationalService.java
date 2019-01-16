/*
 *  Copyright 2019 http://www.hswebframework.org
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package org.hswebframework.web.service.organizational.simple;

import org.hswebframework.web.BusinessException;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.dao.organizational.DepartmentDao;
import org.hswebframework.web.dao.organizational.OrganizationalDao;
import org.hswebframework.web.entity.organizational.DepartmentEntity;
import org.hswebframework.web.entity.organizational.OrganizationalEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.EnableCacheAllEvictTreeSortService;
import org.hswebframework.web.service.organizational.OrganizationalService;
import org.hswebframework.web.service.organizational.event.ClearPersonCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("organizationalService")
@CacheConfig(cacheNames = "organizational")
public class SimpleOrganizationalService extends EnableCacheAllEvictTreeSortService<OrganizationalEntity, String>
        implements OrganizationalService {
    @Autowired
    private OrganizationalDao organizationalDao;

    @Autowired
    private DepartmentDao departmentDao;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public OrganizationalDao getDao() {
        return organizationalDao;
    }

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    @CacheEvict(allEntries = true)
    public OrganizationalEntity deleteByPk(String id) {
        if (DefaultDSLQueryService.createQuery(departmentDao)
                .where(DepartmentEntity.orgId, id)
                .total() > 0) {
            throw new BusinessException("机构下存在部门信息,无法删除");
        }
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.deleteByPk(id);
    }

    @Override
    @CacheEvict(allEntries = true)
    public String insert(OrganizationalEntity entity) {
        entity.setStatus(DataStatus.STATUS_ENABLED);
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.insert(entity);
    }

    @Override
    public int updateByPk(String id, OrganizationalEntity entity) {
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.updateByPk(id, entity);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void disable(String id) {
        Objects.requireNonNull(id);
        createUpdate()
                .set(OrganizationalEntity.status, DataStatus.STATUS_DISABLED)
                .where(OrganizationalEntity.id, id)
                .exec();
        publisher.publishEvent(new ClearPersonCacheEvent());
    }

    @Override
    @CacheEvict(allEntries = true)
    public void enable(String id) {
        Objects.requireNonNull(id);
        createUpdate()
                .set(OrganizationalEntity.status, DataStatus.STATUS_ENABLED)
                .where(OrganizationalEntity.id, id)
                .exec();
        publisher.publishEvent(new ClearPersonCacheEvent());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'code:'+#code")
    public OrganizationalEntity selectByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        return createQuery().where(OrganizationalEntity.code, code).single();
    }

    @Override
    @Cacheable(key = "'name:'+#name")
    @Transactional(readOnly = true)
    public OrganizationalEntity selectByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }

        return createQuery().where(OrganizationalEntity.name, name).single();
    }
}
