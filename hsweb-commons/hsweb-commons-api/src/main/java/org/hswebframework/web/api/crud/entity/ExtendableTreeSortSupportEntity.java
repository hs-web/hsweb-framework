/*
 *
 *  * Copyright 2020 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.api.crud.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;

import javax.persistence.Column;

/**
 * 支持树形结构，排序的实体类，要使用树形结构，排序功能的实体类直接继承该类
 */
@Getter
@Setter
public abstract class ExtendableTreeSortSupportEntity<PK> extends ExtendableEntity<PK>
        implements TreeSortSupportEntity<PK> {
    /**
     * 父级类别
     */
    @Column(name = "parent_id", length = 64)
    @Comment("父级ID")
    @Schema(description = "父节点ID")
    private PK parentId;

    /**
     * 树结构编码,用于快速查找, 每一层由4位字符组成,用-分割
     * 如第一层:0001 第二层:0001-0001 第三层:0001-0001-0001
     */
    @Column(name = "path", length = 128)
    @Comment("树路径")
    @Schema(description = "树结构路径")
    @Length(max = 128, message = "目录层级太深")
    private String path;

    /**
     * 排序索引
     */
    @Column(name = "sort_index", precision = 32)
    @Comment("排序序号")
    @Schema(description = "排序序号")
    private Long sortIndex;

    @Column(name = "_level", precision = 32)
    @Comment("树层级")
    @Schema(description = "树层级")
    private Integer level;


}