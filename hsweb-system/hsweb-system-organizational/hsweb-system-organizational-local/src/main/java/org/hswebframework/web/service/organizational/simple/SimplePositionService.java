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
import org.hswebframework.web.dao.organizational.PersonDao;
import org.hswebframework.web.dao.organizational.PositionDao;
import org.hswebframework.web.entity.organizational.PositionEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.AbstractTreeSortService;
import org.hswebframework.web.service.EnableCacheAllEvictTreeSortService;
import org.hswebframework.web.service.organizational.PositionService;
import org.hswebframework.web.service.organizational.event.ClearPersonCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("positionService")
@CacheConfig(cacheNames = "hsweb-position")
public class SimplePositionService extends EnableCacheAllEvictTreeSortService<PositionEntity, String>
        implements PositionService {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private PositionDao positionDao;

    @Autowired
    private PersonDao personDao;

    @Override
    public PositionDao getDao() {
        return positionDao;
    }

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    @CacheEvict(allEntries = true)
    public PositionEntity deleteByPk(String id) {
        if (!CollectionUtils.isEmpty(personDao.selectByPositionId(id))) {
            throw new BusinessException("岗位中还有人员,无法删除!");
        }
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.deleteByPk(id);
    }

    @Override
    @CacheEvict(allEntries = true)
    public int updateByPk(String id, PositionEntity entity) {
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.updateByPk(id, entity);
    }

    @Override
    @CacheEvict(allEntries = true)
    public String insert(PositionEntity entity) {
        publisher.publishEvent(new ClearPersonCacheEvent());
        return super.insert(entity);
    }
}
