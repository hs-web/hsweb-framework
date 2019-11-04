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


import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.api.crud.entity.RecordCreationEntity;
import org.hswebframework.web.dict.DictDefine;
import org.hswebframework.web.dict.defaults.DefaultDictDefine;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 数据字典
 *
 * @author hsweb-generator-online
 */
@Table(name = "s_dictionary")
@Getter
@Setter
public class DictionaryEntity extends GenericEntity<String> implements RecordCreationEntity {
    //字典名称
    @Column(nullable = false)
    @NotBlank(message = "名称不能为空")
    private String                     name;
    //分类
    @Column(length = 32,name = "classified")
    private String                     classified;
    //说明
    @Column
    private String                     describe;
    //创建时间
    @Column(name = "create_time")
    private Long                       createTime;
    //创建人id
    @Column(name = "creator_id")
    private String                     creatorId;
    //状态
    @Column(name = "status")
    private Byte                       status;

    //字段选项
    private List<DictionaryItemEntity> items;


    public DictDefine toDictDefine(){
       return DefaultDictDefine
                .builder()
                .id(this.getId())
                .alias(this.getName())
                .comments(this.getDescribe())
                .items(this.getItems())
                .build();
    }
}