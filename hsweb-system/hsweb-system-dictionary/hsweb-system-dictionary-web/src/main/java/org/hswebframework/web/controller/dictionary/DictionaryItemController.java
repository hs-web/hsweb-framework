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

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.dictionary.api.DictionaryItemService;
import org.hswebframework.web.dictionary.api.entity.DictionaryItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据字典
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.dictionary-item:dictionary-item}")
@Authorize(permission = "dictionary", description = "数据字典管理")
@Api(value = "数据字典", tags = "数据字典-字典配置")
public class DictionaryItemController implements SimpleGenericEntityController<DictionaryItemEntity, String, QueryParamEntity> {

    @Autowired
    private DictionaryItemService dictionaryService;

    @Override
    public DictionaryItemService getService() {
        return dictionaryService;
    }

}
