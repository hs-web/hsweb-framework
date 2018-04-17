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
package org.hswebframework.web.dictionary.api.entity;

import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.commons.entity.RecordCreationEntity;

/**
 * 数据字典解析配置 实体
 *
 * @author hsweb-generator-online
 */
public interface DictionaryParserEntity extends GenericEntity<String>, RecordCreationEntity {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 值到文本转换方式
     */
    String valueToTextParser = "valueToTextParser";
    /**
     * 文本到值转换方式
     */
    String textToValueParser = "textToValueParser";
    /**
     * 转换失败时的操作
     */
    String onError           = "onError";
    /**
     * 创建时间
     */
    String createTime        = "createTime";
    /**
     * 创建人id
     */
    String creatorId         = "creatorId";
    /**
     * 更新时间
     */
    String updateTime        = "updateTime";
    /**
     * 名称
     */
    String name              = "name";
    /**
     * 说明
     */
    String describe          = "describe";
    /**
     * 分类id
     */
    String classifiedId      = "classifiedId";

    /**
     * @return 值到文本转换方式
     */
    String getValueToTextParser();

    /**
     * 设置 值到文本转换方式
     */
    void setValueToTextParser(String valueToTextParser);

    /**
     * @return 文本到值转换方式
     */
    String getTextToValueParser();

    /**
     * 设置 文本到值转换方式
     */
    void setTextToValueParser(String textToValueParser);

    /**
     * @return 转换失败时的操作
     */
    String getOnError();

    /**
     * 设置 转换失败时的操作
     */
    void setOnError(String onError);

    /**
     * @return 更新时间
     */
    Long getUpdateTime();

    /**
     * 设置 更新时间
     */
    void setUpdateTime(Long updateTime);

    /**
     * @return 名称
     */
    String getName();

    /**
     * 设置 名称
     */
    void setName(String name);

    /**
     * @return 说明
     */
    String getDescribe();

    /**
     * 设置 说明
     */
    void setDescribe(String describe);

    /**
     * @return 分类id
     */
    String getClassifiedId();

    /**
     * 设置 分类id
     */
    void setClassifiedId(String classifiedId);

}