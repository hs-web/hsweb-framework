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
package org.hswebframework.web.service.dictionary.simple;

import org.hswebframework.web.dao.dictionary.DictionaryParserDao;
import org.hswebframework.web.entity.dictionary.DictionaryEntity;
import org.hswebframework.web.entity.dictionary.DictionaryItemEntity;
import org.hswebframework.web.entity.dictionary.DictionaryParserEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.dictionary.DictionaryParser;
import org.hswebframework.web.service.dictionary.DictionaryParserService;
import org.hswebframework.web.service.dictionary.builder.DictionaryParserBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("dictionaryParserService")
public class SimpleDictionaryParserService extends GenericEntityService<DictionaryParserEntity, String>
        implements DictionaryParserService {
    @Autowired
    private DictionaryParserDao dictionaryParserDao;

    @Autowired
    private DictionaryParserBuilder dictionaryParserBuilder;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public DictionaryParserDao getDao() {
        return dictionaryParserDao;
    }

    @Override
    public <V> DictionaryParser<V> getParser(DictionaryEntity dict, String parserId) {
        DictionaryParserEntity entity = selectByPk(parserId);
        assertNotNull(entity);
        SimpleDictionaryParser<V> parser = new SimpleDictionaryParser<>();
        parser.setToValueParser(dictionaryParserBuilder.build(entity.getTextToValueParser()));
        parser.setToTextParser(dictionaryParserBuilder.build(entity.getValueToTextParser()));
        return parser;
    }

}
