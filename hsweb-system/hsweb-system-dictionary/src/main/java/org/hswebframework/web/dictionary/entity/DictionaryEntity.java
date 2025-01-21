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


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.api.crud.entity.RecordCreationEntity;
import org.hswebframework.web.crud.generator.Generators;
import org.hswebframework.web.dict.DictDefine;
import org.hswebframework.web.dict.defaults.DefaultDictDefine;
import org.hswebframework.web.i18n.I18nSupportUtils;
import org.hswebframework.web.i18n.LocaleUtils;
import org.hswebframework.web.i18n.MultipleI18nSupportEntity;
import org.hswebframework.web.validator.CreateGroup;

import javax.persistence.Column;
import javax.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.sql.JDBCType;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 数据字典
 *
 * @author hsweb-generator-online
 */
@Table(name = "s_dictionary")
@Comment("数据字典")
@Getter
@Setter
public class DictionaryEntity extends GenericEntity<String> implements RecordCreationEntity, MultipleI18nSupportEntity {
    //字典名称
    @Column(nullable = false)
    @NotBlank(message = "名称不能为空", groups = CreateGroup.class)
    @Schema(description = "字典名称")
    private String name;
    //分类
    @Column(length = 64, name = "classified")
    @Schema(description = "分类标识")
    private String classified;
    //说明
    @Column
    @Schema(description = "说明")
    private String describe;
    //创建时间
    @Column(name = "create_time", updatable = false)
    @Schema(description = "创建时间")
    @DefaultValue(generator = Generators.CURRENT_TIME)
    private Long createTime;
    //创建人id
    @Column(name = "creator_id", updatable = false)
    @Schema(description = "创建人ID")
    private String creatorId;
    //状态
    @Column(name = "status")
    @DefaultValue("1")
    @Schema(description = "状态,0禁用,1启用")
    private Byte status;

    @Column
    @JsonCodec
    @ColumnType(javaType = String.class, jdbcType = JDBCType.LONGVARCHAR)
    private Map<String, Map<String, String>> i18nMessages;

    //字段选项
    private List<DictionaryItemEntity> items;

    public String getI18nName() {
        return getI18nMessage("name", this.name);
    }

    public String getI18nDescribe() {
        return getI18nMessage("describe", this.describe);
    }

    public void putI18nName(String i18nKey) {
        putI18nName(i18nKey, LocaleUtils.getSupportLocales());
    }

    public void putI18nName(String i18nKey,
                            Collection<Locale> locales) {
        this.i18nMessages = I18nSupportUtils
            .putI18nMessages(
                i18nKey, "name", locales, null, this.i18nMessages
            );
    }

    public DictDefine toDictDefine() {
        return DefaultDictDefine
            .builder()
            .id(this.getId())
            .alias(this.getName())
            .comments(this.getDescribe())
            .items(this.getItems())
            .build();
    }
}