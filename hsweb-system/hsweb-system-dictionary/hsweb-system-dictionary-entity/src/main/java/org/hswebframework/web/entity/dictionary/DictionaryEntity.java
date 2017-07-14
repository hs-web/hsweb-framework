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
package org.hswebframework.web.entity.dictionary;

import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.commons.entity.RecordCreationEntity;

import java.util.List;

/**
 * 数据字典 实体
 *
 * @author hsweb-generator-online
 */
public interface DictionaryEntity extends GenericEntity<String>, RecordCreationEntity {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 字典名称
     */
    String name         = "name";
    /**
     * 分类id
     */
    String classifiedId = "classifiedId";
    /**
     * 说明
     */
    String describe     = "describe";
    /**
     * 创建时间
     */
    String createTime   = "createTime";
    /**
     * 创建人id
     */
    String creatorId    = "creatorId";
    /**
     * 状态
     */
    String status       = "status";

    /**
     * @return 字典名称
     */
    String getName();

    /**
     * 设置 字典名称
     */
    void setName(String name);

    /**
     * @return 分类id
     */
    String getClassifiedId();

    /**
     * 设置 分类id
     */
    void setClassifiedId(String classifiedId);

    /**
     * @return 说明
     */
    String getDescribe();

    /**
     * 设置 说明
     */
    void setDescribe(String describe);

    /**
     * @return 状态
     */
    Byte getStatus();

    /**
     * 设置 状态
     */
    void setStatus(Byte enabled);

    List<DictionaryItemEntity> getItems();

    void setItems(List<DictionaryItemEntity> items);

}