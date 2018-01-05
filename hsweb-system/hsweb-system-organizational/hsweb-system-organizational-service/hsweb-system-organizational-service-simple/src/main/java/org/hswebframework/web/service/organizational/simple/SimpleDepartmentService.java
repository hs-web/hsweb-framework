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

import org.hswebframework.web.dao.organizational.DepartmentDao;
import org.hswebframework.web.entity.organizational.DepartmentEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.AbstractTreeSortService;
import org.hswebframework.web.service.EnableCacheAllEvictTreeSortService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.organizational.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

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

    @Override
    public DepartmentDao getDao() {
        return departmentDao;
    }

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    @Cacheable(cacheNames = "'org-id:'+#orgId")
    public List<DepartmentEntity> selectByOrgId(String orgId) {
        return createQuery().where(DepartmentEntity.orgId, orgId).listNoPaging();
    }

    @Override
    @Cacheable(cacheNames = "'name:'+#name")
    public List<DepartmentEntity> selectByName(String name) {
        return createQuery().where(DepartmentEntity.name, name).listNoPaging();
    }

    @Override
    @Cacheable(cacheNames = "'code:'+#code")
    public DepartmentEntity selectByCode(String code) {
        return createQuery().where(DepartmentEntity.code, code).single();
    }
}
