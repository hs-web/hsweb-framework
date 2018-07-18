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
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.dict.DictDefine;
import org.hswebframework.web.dict.DictDefineRepository;
import org.hswebframework.web.dict.EnumDict;
import org.hswebframework.web.dict.ItemDefine;
import org.hswebframework.web.dictionary.api.DictionaryItemService;
import org.hswebframework.web.dictionary.api.DictionaryService;
import org.hswebframework.web.dictionary.api.entity.DictionaryEntity;
import org.hswebframework.web.dictionary.api.events.ClearDictionaryCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hswebframework.web.controller.message.ResponseMessage.*;

/**
 * 数据字典
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.dictionary:dictionary}")
@Authorize(permission = "dictionary", description = "数据字典管理")
@Api(value = "数据字典", tags = "数据字典-字典配置")
public class DictionaryController implements SimpleGenericEntityController<DictionaryEntity, String, QueryParamEntity> {

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private DictDefineRepository repository;

    @Override
    public DictionaryService getService() {
        return dictionaryService;
    }

    @GetMapping("/define/all")
    @Authorize(merge = false)
    @ApiOperation("获取数据全部字典定义信息")
    public ResponseMessage<List<DictDefine>> getAllDefineById() {
        return ok(repository.getAllDefine());
    }

    @GetMapping("/define/{id:.+}")
    @Authorize(merge = false)
    @ApiOperation("获取数据字典定义信息")
    public ResponseMessage<DictDefine> getDefineById(@PathVariable String id) {
        return ok(repository.getDefine(id));
    }

    @GetMapping("/define/{id:.+}/items")
    @Authorize(merge = false)
    @ApiOperation("获取数据字典选项信息")
    public ResponseMessage<List<EnumDict<Object>>> getItemDefineById(@PathVariable String id) {
        return ok(Optional.ofNullable(repository.getDefine(id))
                .map(DictDefine::getItems)
                .orElse(new java.util.ArrayList<>()));
    }

}
