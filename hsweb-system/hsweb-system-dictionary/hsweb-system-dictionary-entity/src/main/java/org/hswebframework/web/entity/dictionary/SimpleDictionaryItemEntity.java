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

import org.hswebframework.web.commons.entity.SimpleTreeSortSupportEntity;

import java.util.List;

/**
 * 数据字典选项
 *
 * @author hsweb-generator-online
 */
public class SimpleDictionaryItemEntity extends SimpleTreeSortSupportEntity<String> implements DictionaryItemEntity {
    //字典id
    private String dictId;
    //名称
    private String name;
    //字典值
    private String value;
    //字典文本
    private String text;
    //字典值类型
    private String valueType;
    //是否启用
    private Byte   status;
    //说明
    private String describe;
    //快速搜索码
    private String searchCode;

    // 使用表达式拼接text
    // #value+'('+#context.otherVal+')'
    private String textExpression;
    private String valueExpression;


    private List<DictionaryItemEntity> children;

    @Override
    @SuppressWarnings("unchecked")
    public List<DictionaryItemEntity> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<DictionaryItemEntity> children) {
        this.children = children;
    }

    @Override
    public String getTextExpression() {
        return textExpression;
    }

    @Override
    public void setTextExpression(String textExpression) {
        this.textExpression = textExpression;
    }

    @Override
    public String getValueExpression() {
        return valueExpression;
    }

    @Override
    public void setValueExpression(String valueExpression) {
        this.valueExpression = valueExpression;
    }

    /**
     * @return 字典id
     */
    @Override
    public String getDictId() {
        return this.dictId;
    }

    /**
     * 设置 字典id
     */
    @Override
    public void setDictId(String dictId) {
        this.dictId = dictId;
    }

    /**
     * @return 名称
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * 设置 名称
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return 字典值
     */
    @Override
    public String getValue() {
        return this.value;
    }

    /**
     * 设置 字典值
     */
    @Override
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return 字典文本
     */
    @Override
    public String getText() {
        return this.text;
    }

    /**
     * 设置 字典文本
     */
    @Override
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return 字典值类型
     */
    @Override
    public String getValueType() {
        return this.valueType;
    }

    /**
     * 设置 字典值类型
     */
    @Override
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    /**
     * @return 是否启用
     */
    @Override
    public Byte getStatus() {
        return this.status;
    }

    /**
     * 设置 是否启用
     */
    @Override
    public void setStatus(Byte enabled) {
        this.status = enabled;
    }

    /**
     * @return 说明
     */
    @Override
    public String getDescribe() {
        return this.describe;
    }

    /**
     * 设置 说明
     */
    @Override
    public void setDescribe(String describe) {
        this.describe = describe;
    }

    /**
     * @return 快速搜索码
     */
    @Override
    public String getSearchCode() {
        return this.searchCode;
    }

    /**
     * 设置 快速搜索码
     */
    @Override
    public void setSearchCode(String searchCode) {
        this.searchCode = searchCode;
    }

}