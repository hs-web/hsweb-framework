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
package org.hswebframework.web.dictionary.simple;

import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.dictionary.api.DictionaryItemService;
import org.hswebframework.web.dictionary.api.entity.DictionaryItemEntity;
import org.hswebframework.web.dictionary.api.events.ClearDictionaryCacheEvent;
import org.hswebframework.web.dictionary.simple.dao.DictionaryItemDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.AbstractTreeSortService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("dictionaryItemService")
public class SimpleDictionaryItemService extends AbstractTreeSortService<DictionaryItemEntity, String>
        implements DictionaryItemService {
    @Autowired
    private DictionaryItemDao dictionaryItemDao;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public DictionaryItemDao getDao() {
        return dictionaryItemDao;
    }

    @Override
    public String insert(DictionaryItemEntity entity) {
        clearDictCache(entity.getDictId());
        return super.insert(entity);
    }

    @Override
    public int updateByPk(String id, DictionaryItemEntity entity) {
        clearDictCache(entity.getDictId());
        return super.updateByPk(id, entity);
    }

    @Override
    public DictionaryItemEntity deleteByPk(String id) {
        DictionaryItemEntity entity = selectByPk(id);
        if (null != entity) {
            clearDictCache(entity.getDictId());
        }
        return super.deleteByPk(id);
    }

    @Override
    public List<DictionaryItemEntity> selectByDictId(String dictId) {
        if (StringUtils.isNullOrEmpty(dictId)) {
            return new java.util.ArrayList<>();
        }
        return createQuery()
                .where(DictionaryItemEntity.dictId, dictId)
                .orderByAsc(DictionaryItemEntity.sortIndex)
                .listNoPaging();
    }

    private void clearDictCache(String dictId) {
        eventPublisher.publishEvent(new ClearDictionaryCacheEvent(dictId));
    }
}
