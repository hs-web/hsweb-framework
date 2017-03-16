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

package org.hswebframework.web.controller.dictionary;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.GenericEntityController;
import org.hswebframework.web.entity.dictionary.DictionaryParserEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.dictionary.DictionaryParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据字典解析配置
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.dictionary-parser:dictionary-parser}")
@Authorize(permission = "dictionary-parser")
@AccessLogger("数据字典解析配置")
public class DictionaryParserController implements GenericEntityController<DictionaryParserEntity, String, QueryParamEntity, DictionaryParserEntity> {

    private DictionaryParserService dictionaryParserService;

    @Override
    public DictionaryParserEntity modelToEntity(DictionaryParserEntity model, DictionaryParserEntity entity) {
        return model;
    }

    @Autowired
    public void setDictionaryParserService(DictionaryParserService dictionaryParserService) {
        this.dictionaryParserService = dictionaryParserService;
    }

    @Override
    public DictionaryParserService getService() {
        return dictionaryParserService;
    }
}
