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

import org.hswebframework.web.commons.entity.TreeSortSupportEntity;
import org.hswebframework.web.commons.entity.TreeSupportEntity;

import java.util.List;

/**
 * 数据字典选项 实体
 *
 * @author hsweb-generator-online
 */
public interface DictionaryItemEntity extends TreeSortSupportEntity<String> {
 /*-------------------------------------------
    |               属性名常量               |
    ===========================================*/
    /**
     * 字典id
     */
    String dictId         = "dictId";
    /**
     * 名称
     */
    String name           = "name";
    /**
     * 字典值
     */
    String value          = "value";
    /**
     * 字典文本
     */
    String text           = "text";
    /**
     * 字典值类型
     */
    String valueType      = "valueType";
    /**
     * 是否启用
     */
    String status         = "status";
    /**
     * 说明
     */
    String describe       = "describe";
    /**
     * 父级选项
     */
    String parentId       = "parentId";
    /**
     * 树编码
     */
    String path           = "path";
    /**
     * 快速搜索码
     */
    String searchCode     = "searchCode";
    /**
     * 排序索引
     */
    String sortIndex      = "sortIndex";
    /**
     * 树结构层级
     */
    String level          = "level";
    /**
     * 文本提取表达式
     */
    String textExpression = "textExpression";

    /**
     * 文本提取表达式
     */
    String valueExpression = "valueExpression";

    String getTextExpression();

    void setTextExpression(String textExpression);

    String getValueExpression();

    void setValueExpression(String valueExpression);

    /**
     * @return 字典id
     */
    String getDictId();

    /**
     * 设置 字典id
     */
    void setDictId(String dictId);

    /**
     * @return 名称
     */
    String getName();

    /**
     * 设置 名称
     */
    void setName(String name);

    /**
     * @return 字典值
     */
    String getValue();

    /**
     * 设置 字典值
     */
    void setValue(String value);

    /**
     * @return 字典文本
     */
    String getText();

    /**
     * 设置 字典文本
     */
    void setText(String text);

    /**
     * @return 字典值类型
     */
    String getValueType();

    /**
     * 设置 字典值类型
     */
    void setValueType(String valueType);

    /**
     * @return 状态
     */
    Byte getStatus();

    /**
     * 设置 状态
     */
    void setStatus(Byte status);

    /**
     * @return 说明
     */
    String getDescribe();

    /**
     * 设置 说明
     */
    void setDescribe(String describe);

    /**
     * @return 快速搜索码
     */
    String getSearchCode();

    /**
     * 设置 快速搜索码
     */
    void setSearchCode(String searchCode);

    void setChildren(List<DictionaryItemEntity> children);
}