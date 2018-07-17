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
package org.hswebframework.web.dictionary.simple;

import org.hswebframework.web.dictionary.api.DictionaryService;
import org.hswebframework.web.dictionary.api.entity.DictionaryEntity;
import org.hswebframework.web.dictionary.api.events.ClearDictionaryCacheEvent;
import org.hswebframework.web.dictionary.simple.dao.DictionaryDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("dictionaryService")
@CacheConfig(cacheNames = "dictionary")
public class SimpleDictionaryService extends GenericEntityService<DictionaryEntity, String>
        implements DictionaryService {

    @Autowired
    private DictionaryDao dictionaryDao;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public DictionaryDao getDao() {
        return dictionaryDao;
    }

    @Override
    public String insert(DictionaryEntity entity) {
        String id = super.insert(entity);
        eventPublisher.publishEvent(new ClearDictionaryCacheEvent(id));
        return id;
    }

    @Override
    public int updateByPk(String id, DictionaryEntity entity) {
        eventPublisher.publishEvent(new ClearDictionaryCacheEvent(id));
        return super.updateByPk(id, entity);
    }

    @Override
    public DictionaryEntity deleteByPk(String id) {
        eventPublisher.publishEvent(new ClearDictionaryCacheEvent(id));
        return super.deleteByPk(id);
    }
}
