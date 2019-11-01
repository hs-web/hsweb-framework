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
package org.hswebframework.web.dictionary.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.api.crud.entity.GenericTreeSortSupportEntity;
import org.hswebframework.web.dict.EnumDict;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.List;

/**
 * 数据字典选项
 */
@Getter
@Setter
@Table(name = "s_dictionary_item",indexes = {
        @Index(name = "idx_dic_item_dic_id",columnList = "dict_id"),
        @Index(name = "idx_dic_item_ordinal",columnList = "ordinal"),
        @Index(name = "idx_dic_item_path",columnList = "path")
})
public class DictionaryItemEntity extends GenericTreeSortSupportEntity<String> implements EnumDict<String> {
    //字典id
    @Column(name = "dict_id", length = 32, updatable = false, nullable = false)
    private String dictId;
    //名称
    @Column
    private String name;
    //字典值
    @Column
    private String value;
    //字典文本
    @Column
    private String text;
    //字典值类型
    @Column(name = "value_type")
    private String valueType;
    //是否启用
    @Column
    private Byte status;
    //说明
    @Column
    private String describe;

    //快速搜索码
    @Column(name = "search_code")
    private String searchCode;

    @Column(name = "ordinal", nullable = false, updatable = false)
    private Integer ordinal;

    @Override
    public int ordinal() {
        return ordinal == null ? 0 : ordinal;
    }

    private List<DictionaryItemEntity> children;

    @Override
    public Object getWriteJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", getId());
        jsonObject.put("name", getName());
        jsonObject.put("dictId", getDictId());
        jsonObject.put("value", getValue());
        jsonObject.put("text", getText());
        jsonObject.put("ordinal", getOrdinal());
        jsonObject.put("sortIndex", getSortIndex());
        jsonObject.put("parentId", getParentId());
        jsonObject.put("path", getPath());
        jsonObject.put("mask", getMask());
        jsonObject.put("searchCode", getSearchCode());
        jsonObject.put("status", getStatus());
        jsonObject.put("describe", getDescribe());
        return jsonObject;
    }
}