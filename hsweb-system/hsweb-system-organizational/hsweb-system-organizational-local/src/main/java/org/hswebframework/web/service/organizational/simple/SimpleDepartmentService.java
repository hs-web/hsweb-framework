/*
 *  Copyright 2016 http://www.hswebframework.org
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

import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.dao.organizational.DepartmentDao;
import org.hswebframework.web.dao.organizational.PositionDao;
import org.hswebframework.web.entity.organizational.DepartmentEntity;
import org.hswebframework.web.entity.organizational.OrganizationalEntity;
import org.hswebframework.web.entity.organizational.PositionEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.AbstractTreeSortService;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.EnableCacheAllEvictTreeSortService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.organizational.DepartmentService;
import org.hswebframework.web.service.organizational.OrganizationalService;
import org.hswebframework.web.service.organizational.event.ClearPersonCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("departmentService")
@CacheConfig(cacheNames = "department")
public class SimpleDepartmentService
        extends EnableCacheAllEvictTreeSortService<DepartmentEntity, String>
        implements DepartmentService {
    @Autowired
    private DepartmentDao departmentDao;

    @Autowired
    protected PositionDao positionDao;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private OrganizationalService organizationalService;

    @Override
    public DepartmentDao getDao() {
        return departmentDao;
    }

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    @Cacheable(key = "'org-id:'+#orgId")
    public List<DepartmentEntity> selectByOrgId(String orgId) {
        return createQuery().where(DepartmentEntity.orgId, orgId).listNoPaging();
    }

    @Override
    @Cacheable(key = "'org-ids:'+#orgId==null?0:orgId.hashCode()+'_'+#children+'_'+#parent")
    public List<DepartmentEntity> selectByOrgIds(List<String> orgId, boolean children, boolean parent) {
        if (CollectionUtils.isEmpty(orgId)) {
            return new java.util.ArrayList<>();
        }
        Set<String> allOrgId = new HashSet<>(orgId);

        if (children) {
            allOrgId.addAll(orgId.stream()
                    .map(organizationalService::selectAllChildNode)
                    .flatMap(Collection::stream)
                    .map(OrganizationalEntity::getId)
                    .collect(Collectors.toSet()));

        }
        if (parent) {
            allOrgId.addAll(orgId.stream()
                    .map(organizationalService::selectParentNode)
                    .flatMap(Collection::stream)
                    .map(OrganizationalEntity::getId)
                    .collect(Collectors.toSet()));
        }

        return createQuery()
                .where()
                .in(DepartmentEntity.orgId, allOrgId)
                .listNoPaging();
    }

    @Override
    @Cacheable(key = "'name:'+#name")
    public List<DepartmentEntity> selectByName(String name) {
        return createQuery().where(DepartmentEntity.name, name).listNoPaging();
    }

    @Override
    @Cacheable(key = "'code:'+#code")
    public DepartmentEntity selectByCode(String code) {
        return createQuery().where(DepartmentEntity.code, code).single();
    }

    @Override
    public DepartmentEntity deleteByPk(String id) {
        if (DefaultDSLQueryService.createQuery(positionDao)
                .where(PositionEntity.departmentId, id)
                .total() > 0) {
            throw new BusinessException("部门下存在职位信息,无法删除!");
        }
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.deleteByPk(id);
    }

    @Override
    public String insert(DepartmentEntity entity) {
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.insert(entity);
    }

    @Override
    public int updateByPk(String id, DepartmentEntity entity) {
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.updateByPk(id, entity);
    }

}
