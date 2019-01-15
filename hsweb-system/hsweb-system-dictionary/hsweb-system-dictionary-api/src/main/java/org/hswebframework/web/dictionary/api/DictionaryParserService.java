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
package org.hswebframework.web.dictionary.api;

import org.hswebframework.web.dictionary.api.entity.DictionaryEntity;
import org.hswebframework.web.dictionary.api.entity.DictionaryParserEntity;
import org.hswebframework.web.service.CrudService;

/**
 * 数据字典解析配置 服务类
 *
 * @author hsweb-generator-online
 */
public interface DictionaryParserService extends CrudService<DictionaryParserEntity, String> {

    <V> DictionaryParser<V> getParser(DictionaryEntity dict, String parserId);


}
